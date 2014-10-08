<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>RDFスキーマ新規登録</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
?>
<h1 class="title">RDFスキーマ新規登録</h1>
<form action="schemaupload.php" method="post" enctype="multipart/form-data">
  ファイル：<br />
  <input type="file" name="upfile" size="30" /><br />
<br />
  <input type="submit" value="アップロード" />
</form>
<?php include 'inc/footer.php'; ?>
</body>
</html>
