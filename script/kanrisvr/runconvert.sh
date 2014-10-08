#!/bin/bash
#

LOG=/var/log/jrrk/runconvert.`date '+%Y%m%d'`.log
exec > >(tee -a ${LOG}) 2>&1

#
echo "[START] `date '+%Y-%m-%d %H:%M:%S'`"


#
_lockfile="/home/adminuser/jrrktool/`basename $0`.lock"
_lockfile2="/home/adminuser/jrrktool/`basename $0`.lock2"
_lockfile3="/home/adminuser/jrrktool/runconvert.dat"
touch -d '-30 minutes' $_lockfile2


if [ "$_lockfile2" -nt "$_lockfile3" ]; then
	echo 'Too old instance found. '
	rm -f $_lockfile
fi

ln -s /dummy $_lockfile 2> /dev/null || { echo 'Cannot run multiple instance.' >&2; exit 9; }
trap "rm $_lockfile; exit" 1 2 3 15
touch $_lockfile3
#


java -cp "/home/adminuser/jrrktool/jar/*" -Dlog4j.configuration=file:///home/adminuser/jrrktool/nulllog4j.properties net.msecnd.vo.jrrk.LodConvert /var/www/html/lodmanage/upload/ /var/www/html/lodmanage/tempout/ /var/www/html/rdf/ /var/www/html/lodmanage/failed/ /var/www/html/schema/ /var/www/html/lodmanage/commonschema/ /var/www/html/lodmanage/ignorelist/ http://example.org/ false false false http://example.org/res/
echo [convert]Convert proc Done
chmod -f a+rw /var/www/html/lodmanage/tempout/*
chmod -f a+rw /var/www/html/rdf/*
chmod -f a+rw /var/www/html/lodmanage/failed/*

echo "[END]`date '+%Y-%m-%d %H:%M:%S'`"
rm $_lockfile2
rm $_lockfile
exit 0
