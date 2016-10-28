#!/bin/sh

PWD=$(cd "$(dirname "$0")"; pwd)
LOG="/workspace/logs/scheduler.log"
JAVA='/program/jdk1.7.0_79/bin/java'

MAINCLASS=com.nmtx.scheduler.Application
PID=`ps -ef | grep "$MAINCLASS" | grep -v grep | awk '{print $2}'`

if [ "$PID" -gt 0 ] 2>/dev/null ; then
  echo "{$MAINCLASS} is running, can't start it up again!"
  exit
fi

if [ -f $LOG ]; then
  mv $LOG $LOG.`date +'%Y%m%d-%H%M%S'`
fi

CLASSPATH="$PWD/${project.build.finalName}.${project.packaging}:$PWD/libs/*"

nohup $JAVA -cp $CLASSPATH -Dapplication.environment=${environment} -Ddubbo.shutdown.hook=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=40000 -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.access.file=/program/security/jmxremote.access -Dcom.sun.management.jmxremote.password.file=/program/security/jmxremote.password $MAINCLASS $1 $2 > $LOG 2>&1 &

COUNT=0
while [ "$COUNT" -lt 1 ] ; do
    echo -e ".\c"
    sleep 1
    
    COUNT=`ps -ef | grep "$MAINCLASS" | wc -l`
    
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "[$MAINCLASS] has been started up."
