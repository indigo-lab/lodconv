#!/bin/bash
LOG=/var/log/jrrk/runconvertsync.`date '+%Y%m%d'`.log
exec > >(tee -a ${LOG}) 2>&1

#
echo "[START] `date '+%Y-%m-%d %H:%M:%S'`"
echo [type] check value type: $2

touch /home/adminuser/jrrktool/syncs.dat
java -cp "/home/adminuser/jrrktool/jar/*" -Dlog4j.configuration=file:///home/adminuser/jrrktool/nulllog4j.properties net.msecnd.vo.jrrk.LodConvert $1 /var/www/html/lodmanage/tempout/ /var/www/html/rdf/ /var/www/html/lodmanage/failed/ /var/www/html/schema/ /var/www/html/lodmanage/commonschema/ /var/www/html/lodmanage/ignorelist/ http://example.org/ $2 false false http://example.org/ 
ret=$?
if [ $ret -ne 0 ]; then
   echo [convert]Convert proc fail
else
   echo [convert]Convert proc Success
fi
chmod -f a+rw /var/www/html/lodmanage/tempout/*
chmod -f a+rw /var/www/html/rdf/*
chmod -f a+rw /var/www/html/lodmanage/failed/*
touch /home/adminuser/jrrktool/synce.dat

echo "[END]`date '+%Y-%m-%d %H:%M:%S'`"

exit $ret
