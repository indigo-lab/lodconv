<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>データ削除</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
?>
<h1 class="title">データ削除</h1>
<?php

$flbase = htmlspecialchars($_POST['filename']);

$tk = $_POST['tk'];

if($tk==''){
	print('不正な遷移です。');
}else{
//	簡易的なToken
	$dateT = time();

	$date1str = date('YmdHi');					//now
	$date2str = date('YmdHi', $dateT - 60);		//now-1m
	$date3str = date('YmdHi', $dateT - 120);	//now-2m

	$plstr1 = $flbase.'/'.$date1str;
	$plstr2 = $flbase.'/'.$date2str;
	$plstr3 = $flbase.'/'.$date3str;

	$tk1 = md5('QXAYLOQPZQE'.$plstr1);
	$tk2 = md5('QXAYLOQPZQE'.$plstr2);
	$tk3 = md5('QXAYLOQPZQE'.$plstr3);
	
//	echo $tk1."<br/>";
//	echo $tk2."<br/>";
//	echo $tk3."<br/>";

	if($tk1 != $tk && $tk2 != $tk&& $tk3 != $tk){
		print('セッションの時間切れです。再度一覧から実行してください。');
	}else  if(0 != strlen($flbase)){
		copy($emprdf, $lstrdf."/".$flbase.".rdf");
		exec('rm -f '.escapeshellarg($lstup.$flbase).'.*' );
		exec('rm -f '.escapeshellarg($lstsup.$flbase).'.*' );
		exec('rm -f '.escapeshellarg($lsterr.$flbase).'.*' );
		print('データ:'.$flbase.'を削除しました。');
	}else{
		print('データ:'.$flbase.'が見つかりません。');
	}
}
?>
<?php include 'inc/footer.php'; ?>
</body>
</html>
