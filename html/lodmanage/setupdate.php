<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>管理トップ</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';

$uparray  = array();
$upsdfarray  = array();
$suparray = array();
$rdfarray = array();
$farray   = array();
$upallarray = array();

$usesdfarray  = array();

$scarray = array();


##############################################
# csv(+SDFjson)
##############################################

clearstatcache();
$drc=dir($lstup);
while($fl=$drc->read()) {
  $lfl = $lstup."/".$fl;
  if(!(is_dir($lfl) || $fl==".." || $fl==".")){
    $flr=  pathinfo($fl, PATHINFO_FILENAME);
    $flexp=pathinfo($fl, PATHINFO_EXTENSION);
  	$fts = date($dateformat, filemtime($lfl));
    if($flexp=='json'){//	SDF Json のみflで入れる
       	$upsdfarray[$flr]=$fl;
    	$uparray[$fl]=$fl;
    	$upallarray[$fl]=$fts;
	}else{
    	$uparray[$flr]=$fl;
    	$upallarray[$flr]=$fts;
    }
  }
}
$drc->close();



##############################################
# csv(sync)+merge
##############################################

$drc=dir($lstsup);
while($fl=$drc->read()) {
  $lfl = $lstsup."/".$fl;
  if(!(is_dir($lfl) || $fl==".." || $fl==".")){
    $flr=  pathinfo($fl, PATHINFO_FILENAME);
    $fts = date($dateformat, filemtime($lfl));
    $suparray[$flr]=$fl;
    $upallarray[$flr]=$fts;
    
    $flexp=pathinfo($fl, PATHINFO_EXTENSION);
  }
}
$drc->close();

#	time
#asort($upallarray);
#	filename
ksort($upallarray);

##############################################
# rdf (w size check)
##############################################

$drc=dir($lstrdf);
while($fl=$drc->read()) {
  $lfl = $lstrdf."/".$fl;
  if(!(is_dir($lfl) || $fl==".." || $fl==".")){
    $fz=filesize($lfl);
    if($fz > $emprdfsize){
      $flr=pathinfo($fl, PATHINFO_FILENAME);
      $rdfarray[$flr]=$fl;
    }
  }
}
$drc->close();

##############################################
# err
##############################################

$drc=dir($lsterr);
while($fl=$drc->read()) {
  $lfl = $lsterr."/".$fl;
  if(!(is_dir($lfl) || $fl==".." || $fl==".")){
    $flr=  pathinfo($fl, PATHINFO_FILENAME);
    $farray[$flr]=$fl;
  }
}
$drc->close();

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

##############################

print("<h1>データ一覧</h1>");
print('<hr/> <a href="form.php">データ新規登録</a> <a href="search.php">検索</a><hr/>');

print("<table border='1'>");
print("<tr><th>データ名</th><th>更新日時</th><th>ステータス</th><th>変換前</th>");
print("<th>変換後</th><th>エラー内容</th><th>操作</th></tr>");
foreach ($upallarray as $flr => $fts) {
 if(isset($uparray[$flr])){
  $uplst=$lstup;
  $upfl=$uparray[$flr];
 }else{
  $uplst=$lstsup;
  $upfl=$suparray[$flr];
 }
 $uplfl = $uplst."/".$upfl;
 $fz=filesize($lfl);
 
 if(isset($usesdfarray[$flr])){	//	jsonの方がベース名より後に来ることが前提
   continue;
 }
 
 $flexp=pathinfo($flr, PATHINFO_EXTENSION);
 
 $procJson = ('json' === $flexp);
 $procSDFCSV = isset($upsdfarray[$flr]);
 
 print("<tr><td>");
 print($flr);
 print("</td><td> ");
 print($fts);

 $status="登録中";
 $statuscol="gray";
//	ErrorCheck
 if(isset($farray[$flr])){
  $status="登録失敗";
  $statuscol="red";
 }else{
//	RDFCheck
  if(isset($rdfarray[$flr])){
   $status = "登録済";
   $statuscol="green";
  }
 }
 print("</td><td><span style='color:".$statuscol.";'>".$status."</span>");
 
  if($procSDFCSV){	//	SDFの場合
      print("</td><td>");
      print("<input type='button' value='表示(csv)' onclick='window.open(\"".$uplst.$upfl."\")' />");

      print("<input type='button' value='表示(json)' onclick='window.open(\"".$uplst.$upsdfarray[$flr]."\")' />");
      $usesdfarray[$flr.'.json']=$flr;
  
  }else if( $procJson ){ // SDFヘッダのみ
      print("</td><td><input type='button' value='表示(json)' onclick='window.open(\"".$uplst.$upfl."\")' />");
      $flr = pathinfo($flr, PATHINFO_FILENAME);
  
  }else{
    print("</td><td><input type='button' value='表示' onclick='window.open(\"".$uplst.$upfl."\")' />");
  }
 
 if(isset($rdfarray[$flr])){
  print("</td><td><input type='button' value='表示' onclick='window.open(\"".$lstrdf.$rdfarray[$flr]."\")' />");
 }else{
  print("</td><td><br/>");
 }
 if(isset($farray[$flr])){
  print("</td><td><input type='button' value='表示' onclick='window.open(\"".$lsterr.$farray[$flr]."\")' />");
  }else{
  print("</td><td><br/>");
 }

# del
$dellabel='削除';
if($procSDFCSV){
	$dellabel='削除(csv,json)';
}

 print("</td><td><form style='display: inline'  action='wdel.php' method='post'><input type='submit' value='".$dellabel."'/><input type='hidden' name='filename' value='".$flr."'/></form>");

# update
if($procSDFCSV){
 print("<form  style='display: inline' action='form2.php' method='post'><input type='submit' value='更新(csv)'/><input type='hidden' name='filename' value='".$flr.".csv'/></form>");
 print("<form  style='display: inline' action='form2.php' method='post'><input type='submit' value='更新(json)'/><input type='hidden' name='filename' value='".$flr.".json'/></form>");
	
}else{
 print("<form  style='display: inline' action='form2.php' method='post'><input type='submit' value='更新'/><input type='hidden' name='basename' value='".$flr."'/></form>");
}
if(isset($suparray[$flr])){
#rebuild
 print("<form style='display: inline'  action='rebuild.php' method='post'><input type='submit' value='再変換'/><input type='hidden' name='filename' value='".$upfl."'/></form>");
}

 print("</td></tr>\n");
}
print("</table><hr/>\n");


###############################################
#  schema
###############################################
/*
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
*/
?>
<!--
<hr/>
-->
<!--
<a href="form.php">データ登録</a> <a href="search.php">検索</a> <a href="schemaform.php">スキーマ登録</a>
-->
<a href="schematop.php">RDFスキーマ管理</a>

<div align="right">
<a href="logout.php" onClick="if(window.confirm('ログアウトしますか')){return true;}return false;">ログアウト</a>
</div>

<hr/>
<div align="right">このページの最終更新:<?php echo date($dateformat); ?></div>
<!-- 研修環境はコメント	-->
<?php include 'inc/footer.php'; ?>
<!-- 研修環境はコメント	-->
</body>
</html>
