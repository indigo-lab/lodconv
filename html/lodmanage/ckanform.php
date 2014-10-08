<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>新規登録(CKAN)</title>
</head>
<body class='ckanbody' >
<?php
include 'inc/const.php';
include 'inc/header.php';
?>
<h1 class="title">新規登録(CKAN)</h1>
<form action="ckanupload.php" method="post" enctype="multipart/form-data">
<table border='1'>
<tr><th>ファイル</th><td><input type="file" name="upfile" size="30" /></td></tr>
<tr><th>ファイルのエンコード</th><td><select name="encode" size="1">
    <option value="utf8" >UTF-8</option>
    <option value="euc">EUC</option>
    <option value="sjis">SJIS</option>
  </select>
</td></tr>
<tr><th colspan='2'>CKAN連携</th></tr>
<tr><th>CKANサーバのURL</th><td><input name="ckanurl" size="40" value="" /></td></tr>
<tr><th>CKAN 認証キー</th><td><input name="ckankey" size="40"  /></td></tr>
<tr><td colspan='2'><input type="submit" value="アップロード・同期処理" /></td></tr></table>
</form>
<?php include 'inc/footer.php'; ?>
</body>
</html>
