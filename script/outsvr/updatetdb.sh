#!/bin/bash
#


KANRISVR=http://idoloddev0.cloudapp.net
LOG=/var/log/jrrk/updatetdb.`date '+%Y%m%d'`.log
exec > >(tee -a ${LOG}) 2>&1

_lockfile="/home/loduser/jrrktool/`basename $0`.lock"
_lockfile2="/home/loduser/jrrktool/`basename $0`.lock2"
_lockfile3="/home/loduser/jrrktool/update.dat"

touch -d '-30 minutes' $_lockfile2



echo "[START] `date '+%Y-%m-%d %H:%M:%S'`" 
lasttime=0

if [ -a "$_lockfile3" ]; then
    lasttime=`date -r ${_lockfile3} +%s`
fi
echo "[date] " $lasttime

if [ "$_lockfile2" -nt "$_lockfile3" ]; then
        echo 'Too old instance found. '
        rm -f $_lockfile
fi

ln -s /dummy $_lockfile 2> /dev/null || { echo 'Cannot run multiple instance.' >&2; exit 9; }
trap "rm -f $_lockfile; exit" 0 1 2 3 15
#touch $_lockfile3

#
# for rdf data
#

emptyrdfsize=200

cd /home/loduser/jrrktool/updatefile
rm -f *.rdf
rm -f *.txt
wget -r -q -A rdf -nd ${KANRISVR}/listrdf.php?date=${lasttime}
if [ $? -ne 0 ]; then
   echo [rdf]get RDF fail. Exit.
   exit 1
fi

for RDF in `ls *.rdf 2> /dev/null`
do
fsize=`wc -c < $RDF`
if [ $fsize -lt $emptyrdfsize ]; then
  rm -f /var/www/html/rdf/$RDF
  echo [rdf]del $RDF
else
  cp $RDF /var/www/html/rdf/
  echo [rdf]copy $RDF
fi


cd /usr/lib/fuseki
basename=${RDF##*/}
filename=${basename%.*}
./s-put http://localhost:3030/data/data $filename /home/loduser/jrrktool/updatefile/$RDF
if [ $? -ne 0 ]; then
   echo [rdf]TDB update fail. Exit.  $RDF
   exit 1
else
  echo [rdf]TDB update $RDF
fi
cd /home/loduser/jrrktool/updatefile
done

#
# for schema
#

cd /home/loduser/jrrktool/updatefile
rm -f *.rdf
rm -f *.txt
rm -f *.ttl
wget -r -q -A ttl -A rdf -nd ${KANRISVR}/listschema.php?date=${lasttime}
if [ $? -ne 0 ]; then
   echo [schema]get Schema fail. Exit.
   exit 1
fi
for RDF in `ls *.rdf 2> /dev/null`
do
fsize=`wc -c < $RDF `
if [ $fsize -lt $emptyrdfsize ]; then
  rm -f /var/www/html/schema/$RDF
  echo [schema]delete $RDF
else
  cp $RDF /var/www/html/schema/
  echo [schema]copy $RDF
fi

#cd /usr/lib/fuseki
#basename=${RDF##*/}
#filename=${basename%.*}
#./s-put http://localhost:3030/data/data $filename /home/loduser/updatefile/$RDF
#echo [schema]TDB update $RDF
#cd /home/loduser/jrrktool/updatefile
done

for RDF in `ls *.ttl 2> /dev/null`
do
fsize=`wc -c < $RDF `
if [ $fsize -lt $emptyrdfsize ]; then
  rm -f /var/www/html/schema/$RDF
  echo [schema]delete $RDF
else
  cp $RDF /var/www/html/schema/
  echo [schema]copy $RDF
fi

#cd /usr/lib/fuseki
#basename=${RDF##*/}
#filename=${basename%.*}
#./s-put http://localhost:3030/data/data $filename /home/loduser/updatefile/$RDF
#echo [schema]TDB update $RDF
#cd /home/loduser/jrrktool/updatefile
done

echo "[END] `date '+%Y-%m-%d %H:%M:%S'`"

rm $_lockfile2
rm $_lockfile
touch $_lockfile3
exit 0
