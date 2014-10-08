<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>データ更新</title>
<script type="text/javascript">
function syncchg(){
	if(document.reg.sync.checked == true){
		document.reg.valtype.disabled = false;
	}else{
		document.reg.valtype.checked = false;
		document.reg.valtype.disabled = true;
	}
}

</script>

</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
$basename = htmlspecialchars ($_POST['basename']);
$finename = htmlspecialchars ($_POST['filename']);

$filenamemode = (strlen($finename) != 0);
$namestr='basename';
$dispname = $basename;
if($filenamemode){
	$dispname = $finename;
	$namestr='filename';
}

?>
<h1 class="title">データ更新 (<?php echo $dispname; ?>)</h1>
<form name="reg" action="upload2.php" method="post" enctype="multipart/form-data">
<table border='0'>
<tr><td>ファイル選択</td><td><input type="file" name="upfile" size="30" /></td></tr>
<tr><td>ファイルの文字コード</td><td>
<select name="encode" size="1">
    <option value="utf8" >UTF-8</option>
    <option value="euc">EUC</option>
    <option value="sjis">SJIS</option>
</select>
</td></tr>
<tr><td>アップロードと同時に登録を行う</td><td><input type="checkbox" checked name="sync" onChange="syncchg()" /></td></tr>
<tr><td>値の型チェックを行う</br>(アップロードと同時に登録を行う場合のみ選択可)</td><td><input type="checkbox" name="valtype" /></td></tr>
<tr><td colspan='2'><input type="hidden" name="<?php echo $namestr;?>" value="<?php echo $dispname; ?>" /><input type="submit" value="アップロード" /></td></tr>
</table>
</form>
<?php include 'inc/footer.php'; ?>
</body>
</html>
