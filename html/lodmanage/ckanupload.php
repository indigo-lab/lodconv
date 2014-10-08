<?php
include 'inc/const.php';

$sync = 'on';
$encp = $_POST['encode'];

#----------- for CKAN-----------
$openRdfUrlBase = 'http://idoloddev0.cloudapp.net/cdn/rdf/';
$ckanSparqlEndpoint='http://idoloddev0.cloudapp.net/cdn/data/sparql';
$localRdfUrlBase = 'http://localhost/rdf/';
#検索でも使う自分自身のNS
$ownns='http://jrrk.org/yokohama/rdf';

$ckankey= $_POST['ckankey'];
$ckanhost=$_POST['ckanurl'];
$ckanurl = $ckanhost.'api/2/rest/dataset';

#----------------------------
$enc = $enca[$encp];

if($enc){
}else{
	$enc = "UTF-8";
}
$allmsg = "";

if (is_uploaded_file($_FILES["upfile"]["tmp_name"])) {
	if('on' === $sync){
		$updir		= $lstsup;
		$delupdir	= $lstup;
	}else{
		$updir		= $lstup;
		$delupdir	= $lstsup;
	}
	
	$filename=htmlspecialchars($_FILES["upfile"]["name"]);
	$flbase = pathinfo($filename, PATHINFO_FILENAME);
	$flexp=mb_strtolower (pathinfo($filename, PATHINFO_EXTENSION));

	//	拡張子により変換しないものがあるので。
	if($flexp != 'txt' && $flexp != 'csv' && $flexp != 'rdf'){
		$enc="UTF-8";
	}

	$fldir	 = $lstup.$flbase.'.*';
	$fldir_s = $lstsup.$flbase.'.*';
	
	$fldirjson	 = $lstup.$flbase.'.json';
	$fldirjson_s = $lstsup.$flbase.'.json';
	
	$fldirary = glob($fldir);
	$fldir_sary = glob($fldir_s);
	
	$fldirjsonary = glob($fldirjson);
	$fldirjson_sary = glob($fldirjson_s);
	
	$notupoad = false;
	if($flexp == 'json'){
		if(!(count($fldirjsonary) == 0 && count($fldirjson_sary) == 0)){
			$notupoad = true;
		}
	}else{
		if(!(count($fldirary) - count($fldirjsonary) == 0 
		&& count($fldir_sary) - count($fldirjson_sary) == 0)){
			$notupoad = true;
		}
	}

	if($notupoad){
		$allmsg .= "同名ファイルが存在するためアップロードできません。";
		header("HTTP/1.0 400 Bad Request");
	}else{
	
	
	if (move_uploaded_file($_FILES["upfile"]["tmp_name"], $updir . $filename.".t")) {
		$d1=file_get_contents($updir.$filename.".t");
		$d2=mb_convert_encoding($d1, "UTF-8",$enc);
		$handle=fopen($updir.$filename, 'wb');
		fwrite($handle, $d2);
		fclose($handle);
		unlink($updir.$filename.".t");
		chmod($updir . $filename, 0644);

		exec('rm -f '.escapeshellcmd($lsterr.$flbase).'.xml' );
		exec('rm -f '.escapeshellcmd($lstrdf.$flbase).'.rdf' );
		unlink($delupdir.$filename);

		$allmsg .= $filename . "をアップロードしました。<br/>\n";

		if('on' === $sync){
			$ret=null;
			$command=$tooldir.'runconvertsync.sh '.$upfulldir.$filename.' false';

			$output=array();
			exec( $command, $output, $ret );
			if($ret == 0){
				$msg= $filename ."の登録処理を行いました。</br>";

		#------------------------ CKAN -----------------------
		//	CKANへ渡すパラメタ 
				$ckanname   = strtolower($flbase.date('YmdHi'));
				$ckannotes  = 'ckannotes';	//	こちらがdc:description
				$ckantitle  = 'ckantitle';	//	こちらがdc:title
				$ckanauthor = 'ckanauthor';	//	dc:creator
		
				$rdfurl    = $localRdfUrlBase.$flbase.'.rdf';
				$rdfownuri = $ownns.$flbase.'.rdf';

				$data = new SimpleXMLElement(file_get_contents($rdfurl));
				foreach ( $data->children('rdf',TRUE) as $rdfdesc) {
					if($rdfownuri == $rdfdesc['about']){
						$ckantitle	= strval($rdfdesc->children('dc',TURE)->title);
						$ckannotes	= strval($rdfdesc->children('dc',TURE)->description);
						$ckanauthor = strval($rdfdesc->children('dc',TURE)->creator);
					}
				}

				//	公開RDF
				$ckanrdfurl= $openRdfUrlBase.$flbase.'.rdf';
				// CKAN側編集ページ
				$ckanediturl = $ckanhost.'dataset/'.$ckanname;

				$ckanRdfkeyval = array(
					'description'	=> 'RDF',
					'url'			=> $ckanrdfurl,
					'resource_type' => 'RDF',
					'format'		=> 'RDF'
				);
		
				$ckanSpEkeyval = array(
					'description'	=> 'SPARQL Endpoint',
					'url'			=> $ckanSparqlEndpoint,
					'resource_type' => 'api',
					'format'		=> 'api/sparql'
				);
		
				$ckanResKeyval = array(
					$ckanRdfkeyval, $ckanSpEkeyval
				);
		
				$ckankeyval = array(
					'name'		=> $ckanname,
			 		'notes'		=> $ckannotes,
					'title'		=> $ckantitle,
					'author'	=> $ckanauthor,
					'resources' => $ckanResKeyval
				);

				$ckanpost = json_encode($ckankeyval);
		
				$headers = array(
					'Authorization:'.$ckankey
				);
		
				$ch=curl_init();
				curl_setopt ($ch,CURLOPT_URL,$ckanurl);
				curl_setopt ($ch,CURLOPT_POST,1);
				curl_setopt ($ch,CURLOPT_HTTPHEADER, $headers);
				curl_setopt ($ch,CURLOPT_POSTFIELDS,$ckanpost);
				curl_setopt ($ch,CURLOPT_SSL_VERIFYPEER,FALSE);
				curl_setopt ($ch,CURLOPT_RETURNTRANSFER, 1);
				curl_setopt ($ch,CURLOPT_FAILONERROR, TRUE);

				$ckanret = curl_exec($ch);

				if($ckanret == NULL || $ckanret == FALSE ){
					$msg.='<br/>CKAN登録に失敗しました。';
				}else{
					$msg.='<br/>CKAN登録を行いました。';
				}
				curl_close ($ch);
		
		#------------------------ CKAN -----------------------
			 }else if($ret == 1){
				 $msg="フォーマット変換に失敗しました。";
				 header("HTTP/1.0 400 Bad Request");
			 }else if($ret == 2){
				 $msg="バリデートに失敗しました。";
					 header("HTTP/1.0 400 Bad Request");
		 		}else{
		 			$msg="システムでエラーが発生しました。";
		 			header("HTTP/1.0 500Internal Server Error");
		 		}
				$allmsg .=$msg."(エラーコード：".$ret.")<br/>\n";
			}
		} else {
			$allmsg .= "ファイルをアップロードできません。";
			header("HTTP/1.0 500Internal Server Error");
		}
	}
} else {
	$allmsg .= "ファイルが選択されていません。";
}
?><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>データ新規作成(CKAN)</title>
</head>
<body class='ckanbody'>
<?php include 'inc/header.php'; ?>
<h1 class="title">データ新規作成(CKAN)</h1>
<?php
echo $allmsg;
if($ckanret != NULL && $ckanret != FALSE){
	echo '<a href="'.$ckanediturl.'" target="_blank" >CKANへ(別ウインドウ)</a>';
}
include 'inc/footer.php'; ?>
</body>
</html>
