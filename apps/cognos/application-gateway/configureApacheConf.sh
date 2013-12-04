#!/bin/bash -x

serviceDirectory=$1
currPublicIP=$2

export PATH=$PATH:/usr/sbin:/sbin

httpdLocation=`whereis httpd`
for i in ${httpdLocation}
do    
	if [ -d "$i" ] ; then		
		currConf="$i/conf/httpd.conf"
		if [ -f "${currConf}" ] ; then
			service httpd stop	
			chmod -R +rx /root			
			sudo chmod 777 $currConf
			echo "Conf is in ${currConf}"
			sed -i -e "s+c10_location+$serviceDirectory/cognos+g" $serviceDirectory/directives.txt
			cat $serviceDirectory/directives.txt >> ${currConf}
			echo ServerName $currPublicIP:80 >> ${currConf}
			chmod 644 $currConf
			service httpd start
			exit 0
		fi	
    fi	
done

echo "End of $0"