<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
<title>検索結果</title>
</head>
<body>
<?php
include 'inc/const.php';
include 'inc/header.php';
include 'inc/searchlist.php';

$uparray  = array();
$upsdfarray  = array();
$suparray = array();
$rdfarray = array();
$farray   = array();
$upallarray = array();

$scharray = array();

################################################
# search 
################################################
#
$endpoint='http://idoloddev0.cloudapp.net/cdn/data/sparql?query=';
$queryopt='&output=xml';

#ドキュメントのpropを拾う時に使う。ツールが入れる値になる。
#横浜用
$ownns='http://example.org';

# param 
$argprop = $_POST['prop'];
$argval  = htmlspecialchars($_POST['value']);
$argtype = htmlspecialchars($_POST['type']);	//	file,data(default),val
$argtypestr=$searchtype[$argtype];


$shp = '?p';
if(strlen($argprop) > 5){
	$shp='<'.$argprop.'>';
}

//	値チェックする。
if(strlen($argval) == 0){
	echo '検索文字列を入力してください';
}else{


$querystrbase = 'SELECT ?s ?g WHERE{graph ?g{ ?s '.$shp.' ?o . FILTER ( regex(?o,"'.$argval.'")';
$querystrend=' )}}';

if($argtype == 'file'){
	 $querystr = $querystrbase.' && regex(str(?s), "'.$ownns.'") '.$querystrend;
}else{
	 $querystr = $querystrbase.$querystrend;
}

#for debug
#echo  htmlspecialchars($querystr) ;
#

$searchrui = $endpoint.urlencode($querystr).$queryopt;

$xmlstr=file_get_contents($searchrui);

#echo  htmlspecialchars($xmlstr);

$schxml = new SimpleXMLElement($xmlstr);
foreach($schxml->results->result as $result){
	foreach($result->binding as $resultb){
		$attname = $resultb['name'];
		$ruri = $resultb->uri;
		if($attname=='g'){
			$flr=  pathinfo($ruri, PATHINFO_FILENAME);
			$scharray[$flr] = $flr;
		}
	}
}

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
    if($flexp=='json'){//	SDF Json
       	$upsdfarray[$flr]=$fl;
	}else{
    	$fts = date($dateformat, filemtime($lfl));
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

# get uri name

if(isset($searchurilb[$argprop])){
	$argname=$searchurilb[$argprop].'('.htmlspecialchars($argprop).')'; 
}else{
	$argname='('.htmlspecialchars($argprop).')'; 
}

##



##############################

print('<h1 class="title">検索結果</h1>');
print("<ul>");

print(" <li>検索方法:".$argtypestr."</li>");
print(" <li>検索対象述語:".htmlspecialchars($argprop)."</li>");
print(" <li>検索文字列:".$argval."</li>");
print("</ul>");

if(count($scharray) == 0){
	echo '検索結果がありません。';
}else{


print("<table border='1'>");
print("<tr><th>データ名</th><th>更新日時</th><th>ステータス</th><th>変換前</th>");
print("<th>変換後</th><th>操作</th></tr>");
foreach ($upallarray as $flr => $fts) {
	if(!isset($scharray[$flr])){
		continue;
	}
	
 if(isset($uparray[$flr])){
  $uplst=$lstup;
  $upfl=$uparray[$flr];
 }else{
  $uplst=$lstsup;
  $upfl=$suparray[$flr];
 }
 $uplfl = $uplst."/".$upfl;
 $fz=filesize($lfl);
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

 
  if(isset($upsdfarray[$flr])){	//	SDFの場合
      print("</td><td><input type='button' value='表示(csv)' onclick='window.open(\"".$uplst."/".$upfl."\")' />");
      print("<input type='button' value='表示(json)' onclick='window.open(\"".$uplst."/".$upsdfarray[$flr]."\")' />");
  
  
  }else{
    print("</td><td><input type='button' value='表示' onclick='window.open(\"".$uplst."/".$upfl."\")' />");
  }
 
 if(isset($rdfarray[$flr])){
  print("</td><td><input type='button' value='表示' onclick='window.open(\"".$lstrdf."/".$rdfarray[$flr]."\")' />");
 }else{
  print("</td><td><br/>");
 }


# del
$dellabel='削除';
  if(isset($upsdfarray[$flr])){
	$dellabel='削除(csv,json)';
}

 print("</td><td><form style='display: inline'  action='wdel.php' method='post'><input type='submit' value='".$dellabel."'/><input type='hidden' name='filename' value='".$flr."'/></form>");

# update
  if(isset($upsdfarray[$flr])){
 print("<form  style='display: inline' action='form2.php' method='post'><input type='submit' value='更新(csv)'/><input type='hidden' name='filename' value='".$flr.".csv'/></form>");
 print("<form  style='display: inline' action='form2.php' method='post'><input type='submit' value='更新(json)'/><input type='hidden' name='filename' value='".$flr.".json'/></form>");
}else{
 print("<form  style='display: inline' action='form2.php' method='post'><input type='submit' value='更新'/><input type='hidden' name='basename' value='".$flr."'/></form>");
}

#rebuild
if(isset($suparray[$flr])){
 print("<form style='display: inline'  action='rebuild.php' method='post'><input type='submit' value='再変換'/><input type='hidden' name='filename' value='".$upfl."'/></form>");
}
 print("</td></tr>\n");
}
print("</table>\n");
}

}
?>
<hr/>
<a href="search.php">再検索</a>

<hr/>
<div align="right">このページの最終更新:<?php echo date($dateformat); ?></div>
<?php include 'inc/footer.php'; ?>
</body>
</html>
