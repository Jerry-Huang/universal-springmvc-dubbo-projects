#!/bin/sh

PWD=$(cd "$(dirname "$0")"; pwd)

MAINCLASS=com.nmtx.scheduler.Application
PIDS=`ps -ef | grep "$MAINCLASS" | grep -v grep | awk '{print $2}'`

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
        echo "Kill [$MAINCLASS] [PID=$PID] by force!"
      fi
    done

    echo "[$MAINCLASS] [PID=$PID] has been stopped."
  else
    echo "[$MAINCLASS] [PID=$PID] is not running."
  fi
done

echo "[$MAINCLASS] has been stopped."