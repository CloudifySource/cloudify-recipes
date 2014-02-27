#!/bin/bash
set -x -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/epel.sh

sudo yum install -y npm
