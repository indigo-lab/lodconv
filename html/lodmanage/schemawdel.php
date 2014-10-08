<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>スキーマ削除</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
$filename = htmlspecialchars($_POST['filename']);

$datestr = date('YmdHi');
$plstr = $filename.'/'.$datestr;

$tk = md5('XAYLOQPZQE'.$plstr);
//	echo $tk;
?>
<h1 class="title">スキーマ削除</h1>
<form action="schemadel.php" method="post" >
  スキーマファイル名：<br />  <?php echo $filename; ?> を削除します。<input type="hidden" name="filename" value="<?php echo $filename; ?>" />
  <br />
  <input type="hidden" name="tk" value="<?php echo $tk; ?>" />
  <input type="submit" value="削除" /> <input type="button" value="キャンセル" 
  onClick="history.back();" />
</form>
<?php include 'inc/footer.php'; ?>
</body>
</html>
