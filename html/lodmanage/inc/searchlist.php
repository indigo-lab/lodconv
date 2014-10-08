<?php
# For search selectbox

# Name - URI
$searchlburi = array(
 "タイトル(dc)" => "http://purl.org/dc/elements/1.1/title",
 "作成者(dc)"=>"http://purl.org/dc/elements/1.1/creator", 
 "説明(schema)"=> "http://schema.org/description",
 "ラベル(rdfs)"=> "http://www.w3.org/2000/01/rdf-schema#label",
 "タイトル(dcterms)" => "http://purl.org/dc/terms/title",
 "作成者(dcterms)"=>"http://purl.org/dc/terms/creator", 
 "緯度"=>"http://www.w3.org/2003/01/geo/wgs84_pos#lat",
 "経度"=>"http://www.w3.org/2003/01/geo/wgs84_pos#long"

);

# URI - NAME
$searchurilb = array();

foreach($searchlburi as $key => $value){
	$searchurilb[$value] = $key;
}

# SearchType - Name

$searchtype = array("val" => "値検索", "file"=>"データファイルメタデータ検索", "data"=>"ボキャブラリ・値検索");



?>
