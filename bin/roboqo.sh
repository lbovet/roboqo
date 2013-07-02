#!/bin/sh

cd $(dirname $0)
if [ "$1" = "-v" ]; then
echo $1
  verbosity=-debug
fi
export VERTX_OPTS="-Djava.library.path=$PWD/lib -Dlog4j.configuration=log4j${verbosity}.xml $ROBOQO_OPTS"
CLASSPATH=$(echo lib/*.jar | tr ' ' ':')
vertx run li.chee.roboqo.server.ServerVerticle -cp $CLASSPATH
