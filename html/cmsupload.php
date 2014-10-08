<?php
header('Content-type: text/xml');

$err  ="lodmanage/failed/";
$rdf  ="rdf/";

$syncupoalddir= 'lodmanage/syncupload/';
$uploaddir	  = 'lodmanage/upload/';

# 環境に応じて修正　以下はダミーの値
# CMSサーバ等の外部からのアップロード元のIP
# 127.0.0.1は自分自身からのアップロードで使用するため必要。
$allowipar= array('192.168.0.1'=> '','127.0.0.1'=>'');

# CMSサーバ等の外部からのアップロード元のドメイン
$allowhostar= array('test.example.com'=>'','test2.example.jp'=>'');

$ip = $_SERVER["REMOTE_ADDR"];
$host = gethostbyaddr($ip);

#echo $ip;
#echo $host;

$allmsg = "";

if(isset($allowipar[$ip]) || isset($allowhostar[$post])){
	
	$enca = array("euc" => "eucJP-win", "utf8"=>"UTF-8", "sjis"=>"SJIS-win");

	$sync = $_POST['sync'];
	$encp = $_POST['encode'];

	$enc = $enca[$encp];
	if($enc){
	}else{
		$enc="UTF-8";
	}

	$filename=escapeshellcmd($_FILES["upfile"]["name"]);
	$basename=	pathinfo($filename, PATHINFO_FILENAME);
	if (is_uploaded_file($_FILES["upfile"]["tmp_name"]) && 0 != strlen($basename)) {
		if('on' === $sync){
			$updir=$syncupoalddir;
			$delupdir=$uploaddir;
		  }else{
			$updir=$uploaddir;
			$delupdir=$syncupoalddir;
		  }
		  if (move_uploaded_file($_FILES["upfile"]["tmp_name"], $updir . $filename.".t")) {
			$d1=file_get_contents($updir.$filename.".t");
			$d2=mb_convert_encoding($d1, "UTF-8",$enc);
			$handle=fopen($updir.$filename,'wb');
			fwrite($handle, $d2);
			fclose($handle);
			unlink($updir.$filename.".t");
			chmod($updir.$filename, 0644);

			unlink($delupdir.$filename);
			exec('rm -f '.$err.$basename.'.*' );
			exec('rm -f '.$rdf.$basename.'.*' );

			if('on' === $sync){
				$ret=null;
				$command='/home/adminuser/jrrktool/runconvertsync.sh '.'/var/www/html/lodmanage/syncupload/'.$filename;

				$output=array();
				exec( $command, $output, $ret );
				if($ret == 0){
					$msg= "success ".$filename;
				}else if($ret == 1){
					header("HTTP/1.0 400 Bad Request");
					$msg= "convert failed";
				}else if($ret == 2){
					 header("HTTP/1.0 400 Bad Request");
					 $msg= "validate failed";
				}else{
					$msg= "upload failed";
					 header("HTTP/1.0 500 Internal Server Error");
				}
				$allmsg .="<p>".$msg."(".$ret.")</p>";
				foreach ($output as $dval) {
					if(strncmp($dval,'<p>',3) == 0){
						$allmsg .=$dval;
					}
				}
			}
		} else {
			$allmsg .= "<p>not uploaded</p>";
		}
	} else {
		$allmsg .= "<p>not uploaded</p>";
	}
}else{
	header("HTTP/1.0 401 Unauthorized");
	$allmsg .= "<p>not allowed</p>";
}
?><?xml version="1.0" encoding="UTF-8" ?>
<div class="error">
<?php
echo $allmsg;
?>
</div>
