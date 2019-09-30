#!/bin/sh
export MAVEN_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false"
cd .. 
mvn clean install -DskipTests
cd test
mvn clean install -Dtest=com.ag.cache.loadtest.LoadTest