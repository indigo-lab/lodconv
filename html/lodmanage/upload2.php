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

//	for sdf
$argFilename= htmlspecialchars($_POST['filename']);
$argFileExp = pathinfo($argFilename, PATHINFO_EXTENSION);

$basename = htmlspecialchars($_POST['basename']);
$file_nm = htmlspecialchars($_FILES['upfile']['name']);

$extension = pathinfo($file_nm, PATHINFO_EXTENSION);
$filename = $basename.'.'.$extension;

if(strlen($argFilename)!=0){
	$basename  = pathinfo($argFilename, PATHINFO_FILENAME);
	$filename = $argFilename;
}


if (is_uploaded_file($_FILES["upfile"]["tmp_name"])) {
if(strlen($argFileExp) != 0 && $extension != $argFileExp  )	{
	  $allmsg .= "SDFでは違う拡張子のファイルの更新はできません。";
	  
}else if(strlen($argFileExp) != 0 && 'on' == $sync  )	{
	  $allmsg .= "SDFでは登録予約での作業が必要です。";
	
}else if($extension != 'txt' && $extension != 'csv' && $extension != 'rdf' && $extension != 'ttl' && $extension != 'json' ){
  $allmsg .= "サポート外のファイルです。";
}else if('on' == $sync && $extension === 'json'){
  $allmsg .= "JSONファイルは登録予約での作業が必要です。";
}else{
	
	
	
  if('on' === $sync){
    $updir=$lstsup;
    $delupdir=$lstup;
  }else{
    $updir=$lstup;
    $delupdir=$lstsup;
  }
	//	拡張子により変換しないものがあるので。
	if($extension != 'txt' && $extension != 'csv' && $extension != 'rdf'){
			$enc="UTF-8";
	}
  
  
  if (move_uploaded_file($_FILES["upfile"]["tmp_name"], $updir . $filename.".t")) {
    $d1=file_get_contents($updir.$filename.".t");
    $d2=mb_convert_encoding($d1, "UTF-8",$enc);
    $handle=fopen($updir.$filename,'wb');
    fwrite($handle, $d2);
    fclose($handle);
    unlink($updir.$filename.".t");
    chmod($updir . $filename, 0644);
# delete other
    unlink($delupdir.$filename);

    exec('rm -f '.escapeshellcmd($lsterr.$basename).'.xml' );
    exec('rm -f '.escapeshellcmd($lstrdf.$basename).'.rdf' );
    
    $allmsg .= $filename . "を更新しました。";

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
            $allmsg .=$dval;
        }
      }
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
<title>データ更新</title>
</head>
<body>
<?php include 'inc/header.php'; ?>
<h1 class="title">データ更新</h1>
<?php
echo $allmsg;
include 'inc/footer.php'; ?>
</body>
</html>
