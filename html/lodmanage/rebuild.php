<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>データ再変換</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
$finename = htmlspecialchars ($_POST['filename']);

?>
<h1 class="title">データ再変換 (<?php echo $finename; ?>)</h1>
<form action="rebuildproc.php" method="post">
<table border='0'>
<tr><td colspan="2">※同期変換で再変換を行います</td></tr>
<tr><td>値の型チェックを行う</td>
<td><input type="checkbox" name="valtype" /></td></tr>
<tr><td colspan='2'><input type="hidden" name="filename" value="<?php echo $finename; ?>" /><input type="submit" value="再変換" /></td></tr>
</table>
</form>
<?php include 'inc/footer.php'; ?>
</body>
</html>
