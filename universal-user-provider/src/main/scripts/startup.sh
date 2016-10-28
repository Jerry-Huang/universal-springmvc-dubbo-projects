#!/bin/sh

PWD=$(cd "$(dirname "$0")"; pwd)
LOG="/workspace/logs/user-provider.out"
JAVA='/program/jdk1.7.0_79/bin/java'

PORT=${mvn.dubbo.protocol.port}
MAINCLASS=com.nmtx.provider.user.Application
PID=`netstat -tlnp | grep "$PORT" | awk '{print $7}' | awk -F '/' '{print $1}'`
if [ ! $PID ]; then
  PID=`ps -ef | grep "$MAINCLASS" | grep -v grep | awk '{print $2}'`
fi

if [ "$PID" -gt 0 ] 2>/dev/null ; then
  echo "{$MAINCLASS} [$PORT] is running, can't start it up again!"
  exit
fi

if [ -f $LOG ]; then
  mv $LOG $LOG.`date +'%Y%m%d-%H%M%S'`
fi

CLASSPATH="$PWD/${project.build.finalName}.${project.packaging}:$PWD/libs/*"

nohup $JAVA -cp $CLASSPATH -Dapplication.environment=${environment} -Ddubbo.shutdown.hook=true -Ddubbo.registry.file=$HOME/.dubbo/dubbo-registry-${environment}-${mvn.dubbo.protocol.port}.cache -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$((PORT+10000)) -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.access.file=/program/security/jmxremote.access -Dcom.sun.management.jmxremote.password.file=/program/security/jmxremote.password $MAINCLASS > $LOG 2>&1 &

COUNT=0
while [ "$COUNT" -lt 1 ] ; do
    echo -e ".\c"
    sleep 1
    
    COUNT=`netstat -tln | grep "$PORT" | wc -l`
    
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "[$MAINCLASS] [$PORT] has been started up."
