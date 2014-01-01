#!/bin/bash

BENCHMARK_BIN=$1
GRID_NAME=$2
POJOS_NUMBER=$3
LOCATORS=$4

echo "$HOSTNAME"
echo -n "login: "
read USER
exec su -l -c '${BENCHMARK_BIN}/run.sh -url jini://*/*/${GRID_NAME}?locators=${LOCATORS} -i ${POJOS_NUMBER}' $USER

${BENCHMARK_BIN}/run.sh -url jini://*/*/${GRID_NAME}?locators=${LOCATORS} -i ${POJOS_NUMBER}



