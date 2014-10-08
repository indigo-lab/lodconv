<?php
# Date/time format
date_default_timezone_set('Asia/Tokyo');

$dateformat = "Y/m/d H:i:s";

# Dir. (cmd)
$tooldir = '/home/adminuser/jrrktool/';

# Dir. (html)
$upfulldir = '/var/www/html/lodmanage/syncupload/';


# Dir. (base lodmanage/)

$lstrdf  = "../rdf/";
$lstup   = "./upload/";
$lstsup  = "./syncupload/";
$lsterr  = "./failed/";

$lstsc   = "../schema/";


# Empty rdf file

$emprdf = "./empty.rdf";
$emprdfsize=200;


# File encode

$enca = array("euc" => "eucJP-win", "utf8"=>"UTF-8", "sjis"=>"SJIS-win");

?>
