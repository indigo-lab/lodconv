#!/bin/bash
while :
do
cd /usr/lib/fuseki/
./fuseki-server --config=jrrk-tdb.ttl --timeout=20000 
done
