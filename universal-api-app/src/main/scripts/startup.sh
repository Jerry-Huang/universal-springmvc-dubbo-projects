#!/bin/sh

PORT=8094
TOMCAT=tomcat8-$PORT
TOMCAT_CONDITION_CODE="/program/$TOMCAT/bin/tomcat-juli.jar"

PID=`netstat -tlnp | grep "$PORT" | awk '{print $7}' | awk -F '/' '{print $1}'`
if [ ! $PID ]; then
  PID=`ps -ef | grep "$TOMCAT_CONDITION_CODE" | grep -v grep | awk '{print $2}'`
fi

if [ "$PID" -gt 0 ] 2>/dev/null ; then
  echo "[$TOMCAT] [$PORT] is running, can't start it up again!"
  exit
fi

export JAVA_OPTS="-Ddubbo.shutdown.hook=true -Dapplication.environment=${environment} -Ddubbo.registry.file=$HOME/.dubbo/dubbo-registry-${environment}-${PORT}.cache"
export CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$((PORT+10000)) -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.access.file=/program/security/jmxremote.access -Dcom.sun.management.jmxremote.password.file=/program/security/jmxremote.password"

/program/${TOMCAT}/bin/startup.sh

