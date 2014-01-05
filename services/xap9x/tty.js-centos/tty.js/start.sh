#!/bin/bash
set -x -e

XAB_BIN=$1

export XAP_BIN

tty.js --config gs-config.json --deamonize  &
tty.js --config benchmark-config.json --deamonize &
tty.js --config bin-config.json --deamonize &


