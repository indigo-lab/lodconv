<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>スキーマ新規作成</title>
</head>
<body>
<p><?php
include 'inc/const.php';
include 'inc/header.php';
?>
<h1 class="title">スキーマ新規作成-登録</h1>
<?php

#$encp = $_POST['encode'];
#$enc = $enca[$encp];
#if($enc){
#}else{
$enc="UTF-8";
#}

#same check

$filename=htmlspecialchars ($_FILES["upfile"]["name"]);
$fileexist=file_exists($lstsc.$filename);
if($fileexist){
    $fz=filesize($lstsc.$filename);
    if($fz < $emprdfsize){
        $fileexist=false;
     }
}

$flexp=mb_strtolower (pathinfo($filename, PATHINFO_EXTENSION));

if (is_uploaded_file($_FILES["upfile"]["tmp_name"])) {
if($flexp != 'rdf' &&  $flexp != 'ttl'){
  echo "サポート外のファイルです。";
}else{


  if($fileexist){
	echo  "同名ファイルが存在するためアップロードできません。";
  }else{ 
    if (move_uploaded_file($_FILES["upfile"]["tmp_name"], $lstsc . $filename.".t")) {
      $d1=file_get_contents($lstsc.$filename.".t");
      $d2=mb_convert_encoding($d1, "UTF-8",$enc);
      $handle=fopen($lstsc.$filename,'wb');
      fwrite($handle, $d2);
      fclose($handle);
      unlink($lstsc.$filename.".t");
      chmod( $lstsc.$filename, 0644);
      echo $filename . "をアップロードしました。";
    } else {
      echo "ファイルをアップロードできません。";
    }
  }
}
} else {
  echo "ファイルが選択されていません。";
}

?></p>
<?php include 'inc/footer.php'; ?>
</body>
</html>
