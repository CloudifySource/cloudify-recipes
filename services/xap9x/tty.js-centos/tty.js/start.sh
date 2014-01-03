#!/bin/bash
set -x -e

XAB_BIN=$1

export XAP_BIN

tty.js --config my-config.json --deamonize


