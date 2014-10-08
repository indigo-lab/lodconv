<?php
include 'inc/const.php';

$valtype = $_POST['valtype'];

if($valtype == 'on'){
	$valtype = 'true';
}else{
	$valtype = 'false';
}

$allmsg="";
$filename = htmlspecialchars($_POST['filename']);
$basename = pathinfo($filename, PATHINFO_FILENAME);

$updir=$lstsup;
$delupdir=$lstup;

//	非同期は不要。UI側ではねる。

# delete other
exec('rm -f '.escapeshellcmd($lsterr.$basename).'.xml' );
exec('rm -f '.escapeshellcmd($lstrdf.$basename).'.rdf' );


$ret=null;
$command=escapeshellcmd($tooldir.'runconvertsync.sh '.$upfulldir.$filename.' '.$valtype);
      
$output=array();
exec( $command, $output, $ret );
if($ret == 0){
 $msg="登録処理を行いました。";
}else if($ret == 1){
 $msg="フォーマット変換に失敗しました。";
 header("HTTP/1.0 400 Bad Request");
}else if($ret == 2){
  $msg="バリデートに失敗しました。";
  header("HTTP/1.0 400 Bad Request");
}else{
  $msg="システムでエラーが発生しました。";
  header("HTTP/1.0 500Internal Server Error");
}
$allmsg .=$msg."(".$ret.")\n<hr/>\n";
foreach ($output as $dval) {
  if(strncmp($dval,'<p>',3) == 0){
    $allmsg .=$dval;
  }
}

?><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>データ再変換</title>
</head>
<body>
<?php include 'inc/header.php'; ?>
<h1 class="title">データ再変換</h1>
<?php
echo $allmsg;
include 'inc/footer.php'; ?>
</body>
</html>
