#!/bin/bash
set -x -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

sudo yum -y install npm --enablerepo=epel