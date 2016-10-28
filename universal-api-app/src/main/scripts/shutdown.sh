#!/bin/sh

PORT=8094
TOMCAT=tomcat8-$PORT
TOMCAT_CONDITION_CODE="/program/$TOMCAT/bin/tomcat-juli.jar"
/program/${TOMCAT}/bin/shutdown.sh

sleep 5

PIDS=`netstat -tlnp | grep "$PORT" | awk '{print $7}' | awk -F '/' '{print $1}'`
if [ ! $PIDS ]; then
  PIDS=`ps -ef | grep "$TOMCAT_CONDITION_CODE" | grep -v grep | awk '{print $2}'`
fi

for PID in $PIDS
do
  if [ "$PID" -gt 0 ] 2>/dev/null ; then
  
    kill $PID
    
    TIMES=0
    COUNT=$PID

    while [ $COUNT ] ; do

      echo -e ".\c"
      sleep 1
    
      ((TIMES+=1))
    
      COUNT=`ps -ef | grep $TOMCAT_CONDITION_CODE | grep -v grep | awk '{print $2}'`
      COUNT=`echo "${COUNT[@]}" | grep -wq "$PID" &&  echo "$PID"`

      if [ ! $COUNT ]; then
        break
      fi

      if [ $TIMES -ge 10 ]; then
        kill -9 $PID
        echo "Kill {$TOMCAT} [PORT=$PORT] [PID=$PID] by force!"
      fi
    done
 
    echo "[$TOMCAT] [PORT=$PORT] [PID=$PID] has been stopped."
  else
    echo "[$TOMCAT] [PORT=$PORT] [PID=$PID] is not running."
  fi
done

echo "[$TOMCAT] [PORT=$PORT] has been stopped."