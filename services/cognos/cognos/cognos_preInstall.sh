#!/bin/bash -x

cd $1
wget $2
tar xvf $3
componentType=$4
yumPackages=$5
sudo yum install -y -q $yumPackages


atFolder="linuxi38664h"
atsFile="${atFolder}/response.ats"

sed -i "s/\(I Agree=\).*\$/\1y/" $atsFile
sed -i "s/\(APPDIR=\).*\$/\1cognos/" $atsFile
sed -i "s/\(C8BISRVR_APP=\).*\$/\11/" $atsFile
sed -i "s/\(BACKUP=\).*\$/\11/" $atsFile
sed -i "s/\(C8BISRVR_${componentType}=\).*\$/\11/" $atsFile

