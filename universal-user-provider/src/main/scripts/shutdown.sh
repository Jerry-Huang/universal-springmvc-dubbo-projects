#!/bin/sh

PWD=$(cd "$(dirname "$0")"; pwd)

PORT=${mvn.dubbo.protocol.port}
MAINCLASS=com.nmtx.provider.user.Application
PIDS=`netstat -tlnp | grep "$PORT" | awk '{print $7}' | awk -F '/' '{print $1}'`
if [ ! $PIDS ]; then
  PIDS=`ps -ef | grep "$MAINCLASS" | grep -v grep | awk '{print $2}'`
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

      COUNT=`ps -ef | grep $MAINCLASS | grep -v grep | awk '{print $2}'`
      COUNT=`echo "${COUNT[@]}" | grep -wq "$PID" &&  echo "$PID"`

      if [ ! $COUNT ]; then
        break
      fi

      if [ $TIMES -ge 10 ]; then
        kill -9 $PID
        echo "Kill [$MAINCLASS] [PORT=$PORT] [PID=$PID] by force!"
      fi
    done

    echo "[$MAINCLASS] [PORT=$PORT] [PID=$PID] has been stopped."
  else
    echo "[$MAINCLASS] [PORT=$PORT] [PID=$PID] is not running."
  fi
done

echo "[$MAINCLASS] [PORT=$PORT] has been stopped."