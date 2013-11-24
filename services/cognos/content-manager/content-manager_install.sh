#!/bin/bash -x

cd $1
atFolder="linuxi38664h"
atsFile="${atFolder}/response.ats"

sed -i "s/\(C8BISRVR_CONTENT_DATABASE=\).*\$/\11/" $atsFile

