<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>検索</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
include 'inc/searchlist.php';
?>
<h1 class="title">検索</h1>
<hr/>

<h2>データファイルメタデータ検索</h2>
<p>データファイルに記載されたタイトルなどからデータを検索します。</p>
<form action="searchresult.php" method="post" >
<table><tr><th>メタデータ</th><td>
<select name="prop" size="1">
<?php  
foreach($searchlburi as $key => $value){  
    print('<option value="'.$value.'" >'.$key.'('.$value.')</option>');
}
?>
</select>
</td><th>検索文字列</th><td><input name="value" /></td><td>
<input type="hidden" name="type" value="file" />
<input type="submit" value="検索" /></td></tr></table>
</form>

<hr/>

<h2>ボキャブラリ・値検索</h2>
<p>データファイル中に記載された個別のデータのタイトルなどからデータを検索します。</p>
<form action="searchresult.php" method="post" >


<table><tr><th>メタデータ</th><td>
<select name="prop" size="1">
<?php  
foreach($searchlburi as $key => $value){  
    print('<option value="'.$value.'" >'.$key.'('.$value.')</option>');
}
?>
</select>
</td><th>検索文字列</th><td><input name="value" /></td><td>
<input type="hidden" name="type" value="data" />
<input type="submit" value="検索" /></td></tr></table>
</form>

<hr/>

<h2>値検索</h2>

<form action="searchresult.php" method="post" >

<!--
URI:<input name="prop" />
-->
<table><tr><th>検索文字列</th><td><input name="value" /></td><td>
<input type="hidden" name="type" value="val" />
<input type="submit" value="検索" />
</td></tr></table>
</form>

<?php include 'inc/footer.php'; ?>
</body>
</html>
