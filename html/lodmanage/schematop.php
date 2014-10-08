<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>RDFスキーマ管理トップ</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';

$scarray = array();

clearstatcache();

##############################################
# schema (w size check)
##############################################

$drc=dir($lstsc);
while($fl=$drc->read()) {
  $lfl = $lstsc."/".$fl;
  if(!(is_dir($lfl) || $fl==".." || $fl==".")){
    $fz=filesize($lfl);
    if($fz > $emprdfsize){
      $fts = date($dateformat, filemtime($lfl));
      $scarray[$fl]=$fts;
    }
  }
}
$drc->close();
asort($scarray);


###############################################
#  schema
###############################################

print("<h1>RDFスキーマ</h1>");
print("<table border='1'>");
print("<tr><th>ファイル名</th><th>更新日時</th><th>操作</th></tr>");
foreach($scarray as $fl => $fts) {
  $flr =  pathinfo($fl, PATHINFO_FILENAME);

  print("<tr>");
  print("<td>".$fl."</td><td>".$fts);
  print("</td><td><input type='button' value='表示' onclick='window.open(\"".$lstsc.$fl."\")' />");
  print("<form style='display: inline' action='schemawdel.php' method='post'><input type='submit' value='削除'/><input type='hidden' name='filename' value='".$fl."'/></form>");

  print("<form style='display: inline' action='schemaform2.php' method='post'><input type='submit' value='更新'/><input type='hidden' name='filename' value='".$fl."'/></form>");

  print("</td></tr>\n");
}
print("</table>");

?>
<hr/>
<a href="schemaform.php">RDFスキーマ新規登録</a>

<hr/>
<div align="right">このページの最終更新:<?php echo date($dateformat); ?></div>
<?php include 'inc/footer.php'; ?>
</body>
</html>
