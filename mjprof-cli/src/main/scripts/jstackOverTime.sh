countdown()
{
  countdown=${1:-$(($TIME/$FREQUENCY))}   ## 60-second default
  w=${#countdown}

  while [ $countdown -gt 0 ]
  do
    sleep $FREQUENCY &
    printf "  %${w}d\r" "$TIME"

    now=$(date +"%Y-%m-%d-%T")
    filename="jstack.$now.log"	
    jstack -l $PID  > $DIRECTORY/$filename.txt
    countdown=$(( $countdown - 1 ))
    TIME=$(( $TIME- 1 ))
    wait
  done
  printf "\a"
} 2>/dev/null


if [ ! $1  ]
then
	echo "You must enter command"
else
	PID=$1	
fi
if [ $2 ]
then
	TIME=$2
else
	TIME=60
fi

if [ $3 ]
then
	FREQUENCY=$3
else
	FREQUENCY=5
fi

if [ $4 ]
then
	DIRECTORY=$4
else
	dir_name="jstack.$(date +"%Y-%m-%d-%T").PID.$PID"
	DIRECTORY=/opt/$dir_name
fi

if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

echo 'Running jstack pid ' $PID 'every' $FREQUENCY 'seconds for the next '$TIME ' seconds'
echo 'You can found the output under '$DIRECTORY 

countdown

echo 'finish.'