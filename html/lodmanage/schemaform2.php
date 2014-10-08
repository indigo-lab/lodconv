<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>RDFスキーマ更新</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';

$basefn = htmlspecialchars($_POST['filename']);
?>
<h1 class="title">RDFスキーマ更新(<?php echo $basefn; ?>)</h1>
<form action="schemaupload2.php" method="post" enctype="multipart/form-data">
  ファイル：<br />
  <input type="file" name="upfile" size="30" /><br />
<input type="hidden" name="filename" value="<?php echo $basefn; ?>" />
  <input type="submit" value="アップロード" />
</form>
<?php include 'inc/footer.php'; ?>
</body>
</html>
