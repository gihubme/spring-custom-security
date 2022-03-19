#!/bin/bash
#chmod +x scripts/db/start.sh
#chmod 755 scripts/db/start.sh
#scripts/db/start.sh

dataVol='postgres_data'
containerName='jwt-db'
networkName='app-network'


run () {
 docker start  $containerName
 export CONTAINER_ID=$(sudo docker ps -a | grep jwt-db | head -c12)
 echo $CONTAINER_ID is starting under name: $containerName

 # search for postgres among all running processes
 ps -ef | grep postgres
 sleep 3  # Waits 1 seconds.
 docker exec -it $containerName psql  -p 5432 -U postgres -d acleaneva_database
  #\l;   \c acleaneva_database; \dt;
  # \conninfo; \dn;
  # \x; << toggle vertical output
}

create_and_run () {
  docker volume create $dataVol
  docker network create --driver  bridge $networkName

  docker run -itd --name $containerName \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_DB=acleaneva_database \
  -p 5432:5432 \
  -v $dataVol:/var/lib/postgresql/data \
  --network $networkName \
  -d postgres:14-alpine

  export CONTAINER_ID=$(sudo docker ps -a | grep jwt-db | head -c12)
  echo $CONTAINER_ID is running under name: $containerName

  sleep 3  # Waits 1 seconds.
  docker exec -it $containerName psql  -p 5432 -U postgres -d acleaneva_database

}

# https://stackoverflow.com/questions/38576337/how-to-execute-a-bash-command-only-if-a-docker-container-with-a-given-name-does
# exact match name=^/${CONTAINER_NAME}$ >> name=^jwt-db
# if $containerName is listed >> can be running or not
if [ "$(docker ps -qa -f name=^$containerName)" ];
then
    # if $containerName exists and is not running
    if [ "$(docker ps -aq -f status=exited -f name=^$containerName)" ];
    then
        # run existing
        run
    else
        # do nothing
        export CONTAINER_ID=$(sudo docker ps -a | grep jwt-db | head -c12)
        echo $CONTAINER_ID is already running under name: $containerName
        docker exec -it $containerName psql  -p 5432 -U postgres -d acleaneva_database
    fi

else
    # cleanup
    source $(dirname "$0")/cleanup.sh
    # run your container
    # docker run -d --name $containerName postgres:14-alpine
    create_and_run
fi

# printenv