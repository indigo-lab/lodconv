<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>list</title>
</head>
<body>
<?php
date_default_timezone_set('UTC');

$lst = "./rdf"; 

$reqtime=$_GET['date'];
$reqtime10=intval($reqtime) - 60 * 10;

$drc=dir($lst);
print("<UL>");
while($fl=$drc->read()) {
	$lfl = $lst."/".$fl;
	$din = pathinfo($lfl);
	$fts = intval(date("U",filemtime($lfl)));
	if(!(is_dir($lfl) || $fl==".." || $fl==".")){
		if($fts > $reqtime10){
			print("<LI>");
			print("<a href=".$lst."/".$fl.">".$fl."</a> ");
			echo $fts;
			print("</LI>");
		}
	}
}
print("</UL>");
$drc->close();
?>
</body>
</html>
