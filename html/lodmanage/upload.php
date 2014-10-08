<?php
include 'inc/const.php';

$sync = $_POST['sync'];
$encp = $_POST['encode'];
$valtype = $_POST['valtype'];
if($valtype == 'on'){
	$valtype = 'true';
}else{
	$valtype = 'false';
}

$enc = $enca[$encp];
if($enc){
}else{
	$enc="UTF-8";
}
$allmsg="";

$filename=htmlspecialchars($_FILES["upfile"]["name"]);
$flbase = pathinfo($filename, PATHINFO_FILENAME);
$flexp=mb_strtolower (pathinfo($filename, PATHINFO_EXTENSION));
  

if (is_uploaded_file($_FILES["upfile"]["tmp_name"])) {
if($flexp != 'txt' && $flexp != 'csv' && $flexp != 'rdf' && $flexp != 'ttl' && $flexp !='json'){
  $allmsg .= "サポート外のファイルです。";

}else if('on' == $sync && $flexp === 'json'){

  $allmsg .= "JSONファイルは登録予約での作業が必要です。";

}else{
  if('on' === $sync){
    $updir    = $lstsup;
    $delupdir = $lstup;
  }else{
    $updir    = $lstup;
    $delupdir = $lstsup;
  }


	//	拡張子により変換しないものがあるので。
	if($flexp != 'txt' && $flexp != 'csv' && $flexp != 'rdf'){
			$enc="UTF-8";
	}

  $fldir   = $lstup.$flbase.'.*';
  $fldir_s = $lstsup.$flbase.'.*';
  
  $fldirjson   = $lstup.$flbase.'.json';
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
    $handle=fopen($updir.$filename,'wb');
    fwrite($handle, $d2);
    fclose($handle);
    unlink($updir.$filename.".t");
    chmod($updir . $filename, 0644);

    exec('rm -f '.escapeshellcmd($lsterr.$flbase).'.xml' );
    exec('rm -f '.escapeshellcmd($lstrdf.$flbase).'.rdf' );
    unlink($delupdir.$filename);

    $allmsg .= $filename . "をアップロードしました。\n";

    if('on' === $sync){
      $ret=null;
      $command=escapeshellcmd($tooldir.'runconvertsync.sh '.$upfulldir.$filename.' '.$valtype);

      $output=array();
      exec( $command, $output, $ret );
      if($ret == 0){
        $msg="登録処理を行いました。";
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
      $allmsg .=$msg."(エラーコード：".$ret.")\n<br/>\n";
      foreach ($output as $dval) {
         if(strncmp($dval,'<p>',3) == 0){
            $allmsg .= $dval;
        }
      }
     }
  } else {
    $allmsg .= "ファイルをアップロードできません。";
    header("HTTP/1.0 500Internal Server Error");
  }
 }
 }
} else {
  $allmsg .= "ファイルが選択されていません。";
}
?><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>データ新規作成</title>
</head>
<body>
<?php include 'inc/header.php'; ?>
<h1 class="title">データ新規作成</h1>
<?php
echo $allmsg;
include 'inc/footer.php'; ?>
</body>
</html>
