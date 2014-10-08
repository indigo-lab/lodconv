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
?>
<h1 class="title">スキーマ削除</h1>
<?php
$fl = htmlspecialchars($_POST['filename']);
$flexp=mb_strtolower (pathinfo($fl, PATHINFO_EXTENSION));

$tk = $_POST['tk'];

if($tk==''){
	print('不正な遷移です。');
}else{
	$dateT = time();

	$date1str = date('YmdHi');	//now
	$date2str = date('YmdHi', $dateT - 60);	//now-1m
	$date3str = date('YmdHi', $dateT - 120);	//now-1m

	$plstr1 = $fl.'/'.$date1str;
	$plstr2 = $fl.'/'.$date2str;
	$plstr3 = $fl.'/'.$date3str;

			
	$tk1 = md5('XAYLOQPZQE'.$plstr1);
	$tk2 = md5('XAYLOQPZQE'.$plstr2);
	$tk3 = md5('XAYLOQPZQE'.$plstr3);
	
	if($tk1 != $tk && $tk2 != $tk&& $tk3 != $tk){
		print('セッションの時間切れです。再度一覧から実行してください。');
	}else if($flexp=='ttl'){
        unlink($lstsc.'/'.$fl);
        exec('touch '.escapeshellcmd($lstsc.'/'.$fl) );
        print('ファイル:'.$fl.'を削除しました。');
	}else{
	  if(copy($emprdf, $lstsc."/".$fl)){
		print('ファイル:'.$fl.'を削除しました。');
	  }else{
		print('ファイル:'.$fl.'が見つかりません。');
	  }
	}

}

?>
<?php include 'inc/footer.php'; ?>
</body>
</html>
