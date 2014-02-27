grep -q $1 /etc/hosts
if [ $? -eq 0 ]; then
	exit 0
fi
cat /etc/hosts > /tmp/hosts
echo "$1 $2" >> /tmp/hosts
mv /tmp/hosts /etc/hosts
