#!/bin/bash

BENCHMARK_BIN=$1
GRID_NAME=$2
POJOS_NUMBER=$3
LOCATORS=$4

${BENCHMARK_BIN}/run.sh -url jini://*/*/${GRID_NAME}?locators=${LOCATORS} -i ${POJOS_NUMBER}



