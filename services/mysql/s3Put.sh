#!/bin/bash -x

baseFolder="$1"
s3Folder="$2"
bucketName="$3"
currFilePath="$4"
destFileName="$5"

cd $baseFolder/$s3Folder
rootFolder=`sudo find . -name "s3cmd" | xargs dirname`
$rootFolder/s3cmd --acl-private put $currFilePath s3://$bucketName/$destFileName

echo "End of $0"


