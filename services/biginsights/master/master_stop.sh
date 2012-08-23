#! /bin/bash


echo "master_stop.sh: BigInsights is about to be stopped!!!!!"


$1$2/bin/stop-all.sh
	}
}
rm -Rf /hadoop
rm -Rf $1
userdel biadmin
sed -i '/biginsights/d' ~/.bashrc

echo "master_stop.sh: BigInsights folders cleaned!!!!!"
