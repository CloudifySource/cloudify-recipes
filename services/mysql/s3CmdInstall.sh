#!/bin/bash -x

baseFolder="$1"
s3Folder="$2"
apiKey="$3"
secretKey="$4"
decryptTxt="$5"
sc3Gzip="$6"

echo "$0 yum install -q -y python-magic ..."
sudo yum install -q -y python-magic
cd $baseFolder
rm -rf $s3Folder
mkdir $s3Folder
cd $s3Folder

wget $sc3Gzip
gunzip s3cmd*.gz
tar -xf *.tar
rm -f *.tar
rootFolder=`ls`
cd $rootFolder
touch s3Input
echo $apiKey >s3Input
echo $secretKey >>s3Input
echo "${decryptTxt}">>s3Input
echo "" >>s3Input
echo Yes>>s3Input
echo Y>>s3Input
echo y>>s3Input
./s3cmd --configure < s3Input

if [ $? -gt 0 ] ; then
	# Adding the missing json (pythonmodule)
	wget http://downloads.sourceforge.net/project/json-py/json-py/3_4/json-py-3_4.zip
	unzip -u json-py-3_4.zip
	# removing wrong code - probably due to non-matching versions.
	sed -i "s/,timeout=0.1//" S3/Config.py
	./s3cmd --configure < s3Input
fi

rm -f s3Input


echo "End of $0"