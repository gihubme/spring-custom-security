#!/bin/bash
#chmod +x scripts/db/cleanup.sh
#chmod 755 scripts/db/cleanup.sh
#scripts/db/cleanup.sh

dataVol='postgres_data'
containerName='jwt-db'
networkName='app-network'

clean_all_up () {

  if [ "$(docker ps -qa -f name=^$containerName)" ];
  then
      docker rm -f $containerName
      echo $containerName was removed
#      docker ps -a
  fi

  if [ "$(docker volume ls -q -f name=^$dataVol)" ];
  then
      docker volume rm $dataVol
      echo $dataVol was removed
#      docker volume ls
  fi

  if [ "$(docker network ls -q -f name=^$networkName)" ];
  then
      docker network rm $networkName
      echo $networkName was removed
#      docker network ls
  fi

}

clean_all_up