#!/bin/sh

mvn -Dmaven.failsafe.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" clean install -Dtest=com.ag.cache.multidatasource.TestMultiDataBase
