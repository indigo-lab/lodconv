<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>スキーマ更新</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
?>
<h1 class="title">スキーマ更新-登録</h1>
<p><?php


#$encp = $_POST['encode'];
#$enc = $enca[$encp];
#if($enc){
#}else{
$enc="UTF-8";
#}

$basename =htmlspecialchars ( $_POST['filename']);
$baseexp = pathinfo($basename, PATHINFO_EXTENSION);
$file_nm = htmlspecialchars ($_FILES['upfile']['name']);
$extension = pathinfo($file_nm, PATHINFO_EXTENSION);
$filename = $basename;

if (is_uploaded_file($_FILES["upfile"]["tmp_name"])) {
if($extension != $baseexp){
  echo "更新元ファイルと形式が異なります。";
}else{


  if (move_uploaded_file($_FILES["upfile"]["tmp_name"], $lstsc . $filename.".t")) {
    $d1=file_get_contents($lstsc.$filename.".t");
    $d2=mb_convert_encoding($d1, "UTF-8",$enc);
    $handle=fopen($lstsc.$filename,'wb');
    fwrite($handle, $d2);
    fclose($handle);
    unlink($lstsc.$filename.".t");
    chmod( $lstsc.$filename, 0644);
    echo $filename . "を更新しました。";

  } else {
    echo "ファイルをアップロードできません。";
  }
}
} else {
  echo "ファイルが選択されていません。";
}

?></p>
<?php include 'inc/footer.php'; ?>
</body>
</html>
