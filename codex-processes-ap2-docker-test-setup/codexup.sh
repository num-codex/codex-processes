#!/usr/bin/env sh

echo 'Docker compose up of Codex environment....'

runUntilOutput(){
	touch $OUTPUT
	echo "Output: $OUTPUT"
	# send stderr to the address stdout is at
    $COMMAND > $OUTPUT 2>&1 &
    process_pid=$!
    echo "Process pid: $process_pid"
    echo "Wait:"
    until grep -q -i ' Started @' $OUTPUT
    do       
      if grep -q -i 'java exited with code 1' $OUTPUT
      then
        echo "The process died" >&2
        exit 1
      fi
      echo -n "."
      sleep 1
    done
    echo 
    echo "Process is running!" 
	sleep 5
}


if [ $1 = "clean" ]; then 
	docker-compose down -v
	echo "******** docker-compose down -v ***********"
fi
wait $!

OUTDIR=./logs/
if [ ! -d "$OUTDIR" ]; then
	mkdir $OUTDIR
fi

echo "******** start zars ***********"
docker-compose up -d zars-fhir-proxy
wait $!
sleep 4

COMMAND="docker-compose logs -f zars-fhir-app"
OUTPUT="$OUTDIR""ZarsFHIR.log"
runUntilOutput COMMAND
COMMAND="docker-compose up zars-bpe-app"
OUTPUT="$OUTDIR""ZarsBPE.log"
runUntilOutput COMMAND

echo "******** start DIC-1 ***********"
docker-compose up -d dic-1-fhir-proxy
sleep 8


COMMAND="docker-compose logs -f dic-1-fhir-app"
OUTPUT="$OUTDIR""DIC_1FHIR.log"
runUntilOutput COMMAND
COMMAND="docker-compose up dic-1-bpe-app"
OUTPUT="$OUTDIR""DIC_1BPE.log"
runUntilOutput COMMAND


echo "******** start DIC-2 ***********"
docker-compose up -d dic-2-fhir-proxy
sleep 8

COMMAND="docker-compose logs -f dic-2-fhir-app"
OUTPUT="$OUTDIR""DIC_2FHIR.log"
runUntilOutput COMMAND
COMMAND="docker-compose up dic-2-bpe-app"
OUTPUT="$OUTDIR""DIC_2BPE.log"
runUntilOutput COMMAND

