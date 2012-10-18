#! /bin/bash

if [[ $EUID -ne 0 ]]; then
	echo "Not root, need sudo"		
	if [ ! -d ~/.ssh ]; then
		sudo mkdir "~/.ssh"
	fi		
echo "-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEAxkJMUduPlNYTRhY5HxXqUaR/ENe7jclL4MgZ/hlVphH8v5ioy4yYVEeuExq+
U8Wn+Kt2UwnbfSlXEo1R0izAsQcrEqY3BO7EPNWt+lCIxSKG6tRcEkvLCyzCf3YkoY6r6tdlmD1B
qu7Dp1p5H5TVlbsgWDwrx7zMTKzDWVwfSaWJGcl2Woze93bBA9Hme+qDmICaLhmP2R0T/6cH27tm
KtCzXpBW5JPlSsxNDLRr41giKTZ/GrFs3f0u0atwGO4322GyMVm+WHKCM/fID7KjvAQAe1APjekV
3bD8v5qQMKWnWrN+szInYr1V46oBr+4ysrffzIA+qjDCIi4p9CrzAwIDAQABAoIBAQCG/0PahUeK
UA6FrcItcY3BE+JVxJ/4FrYtE/Pm+rdAQoU4rSlamJxs3vYgGhi5ECClCZOjTVYALrMUddfd5g07
PadNppJWMIK87b7ucAIBQdZedkVPS/6w2ESD6B1fSLe1S//onuVlD/l3rfpFz+FzeBy5GBezhNIT
75uA8GJ5kSKI36FL8dz2p/blwt7pGnYG12wNJJKF0nejFFXOVSLb4WdTHCYIe5p+eB/VTOMyLpLe
SOfM7WprA2gZMCLtjrjulIA6eh2x1vipuQucfW3C+hc/I+rVL2k9kloI3REguzJYJ7JAuu7+CCiW
FpWNqWjbW8zHTO8lwrswkFoJgFlJAoGBAPns+tkkh3VA7zffS0JbUJluFFUvpBX+KucJgFjzYU0j
jGIXKCRB+YyObWFQNpAdXrd3scNVGPTVJ/7kwYJv+vx02DQKxk0kc92GWmkY4ydN+r7n1E6ErR+4
zDWd6SbNOcChoKqAMjW2QAQcB8UNVsFhupdz+ziYBxwl6PUlalvdAoGBAMsT2f0uwHhJdrxJcWIA
KHiIEmeg/7KlchWGJG3rI+tY1GYuNXBOhytCuHr6p/OWAV+PL4gXR3WDfXMWVkyyIAbuVWkRhqpk
eMBtpvpeOzyojSFIbeZr6OLPwfnAP7pG5BuG9poeVFOgx7Ko4lLSwFeg93QNlqxGtiVj1meA7oxf
AoGAEr1UKcf1aDNQPI4/pGAYcQix+Zky4ntGWEB8IS9Okmwh8JDv75gK0CE1zmAnVzL8kSEmcREG
aAU7lH8ui0s+NIPGWlCcgdhD385dJRg6oA3WyU20u5ZzLLe8iCCpBMHKnMkBtQcbvK9HqYc4hev0
H1fml8iYg6vSjKfDCe6eRPkCgYBsdBWfGTDBDLrUo4RiCiOS+1iY72qfRaw/wnwCF+n+7lnAmD0B
1W5qtB9BzkuT0zC4kAeabpRDNg3xQKSmIRrpmK8Uhb+dkDrMycK7Q0fvhTSZ6cyHmmmtd7boYrum
B+YarWFVvYzbfKopx/fWs6b1JYoB+J/XMYxlO4RtknXTkQKBgHDSf/7XLbABz0xF/yzImpAcZeDZ
4B79byZSvgNBvq1tA79C4q10BkDcwyXBxh+4QZ5cJaw+N4Mh9yMlDw7UmHqOSAhZLCyLjDfZnmDa
fH96nalQGtwmDwT0KzGQjBYJQ4hwzYEgvD5DEB/fS55/oHO0bf5a4NMlsf+XitCHlmW1
-----END RSA PRIVATE KEY-----" | sudo tee ~root/.ssh/id_rsa
	sudo sudo chmod 600 ~root/.ssh/id_rsa
	echo "StrictHostKeyChecking no" | sudo tee ~root/.ssh/config
	echo "CheckHostIP no" | sudo tee -a ~root/.ssh/config
	echo "PasswordAuthentication no" | sudo tee -a ~root/.ssh/config
	sudo chmod 600 ~root/.ssh/config
	sudo ssh-keygen -y -f ~root/.ssh/id_rsa | sudo tee ~root/.ssh/id_rsa.pub
	sudo cat ~root/.ssh/id_rsa.pub | sudo tee -a ~root/.ssh/authorized_keys
	echo "sudo cp -R ~root/.ssh ~biadmin/"
	sudo cp -R ~root/.ssh ~biadmin/
	sudo chown -R biadmin.biadmin ~biadmin/.ssh
	sudo ls -al ~biadmin/.ssh
#	sudo cp ~biadmin/.ssh/authorized_keys ~biadmin/.ssh/id_rsa.pub

	public_ip=`curl http://169.254.169.254/latest/meta-data/public-ipv4`
	private_ip=`curl http://169.254.169.254/latest/meta-data/local-ipv4`

	echo "$public_ip master.$DOMAIN master" | sudo tee /etc/hosts
	echo "$private_ip master-internal.$DOMAIN master-internal" | sudo tee /etc/hosts
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		data_node_public_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/public-ipv4"`
		data_node_private_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/local-ipv4"`
		echo "$data_node_public_ip data-1.$2 data-1"| sudo tee -a /etc/hosts
		echo "$data_node_private_ip data-1-internal.$2 data-1-internal" | sudo tee -a /etc/hosts
	done
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		scp /etc/hosts root@$line:/etc/hosts
	done

else
	if [ ! -d ~/.ssh ]; then
		mkdir "~/.ssh"
	fi	
echo "-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEAxkJMUduPlNYTRhY5HxXqUaR/ENe7jclL4MgZ/hlVphH8v5ioy4yYVEeuExq+
U8Wn+Kt2UwnbfSlXEo1R0izAsQcrEqY3BO7EPNWt+lCIxSKG6tRcEkvLCyzCf3YkoY6r6tdlmD1B
qu7Dp1p5H5TVlbsgWDwrx7zMTKzDWVwfSaWJGcl2Woze93bBA9Hme+qDmICaLhmP2R0T/6cH27tm
KtCzXpBW5JPlSsxNDLRr41giKTZ/GrFs3f0u0atwGO4322GyMVm+WHKCM/fID7KjvAQAe1APjekV
3bD8v5qQMKWnWrN+szInYr1V46oBr+4ysrffzIA+qjDCIi4p9CrzAwIDAQABAoIBAQCG/0PahUeK
UA6FrcItcY3BE+JVxJ/4FrYtE/Pm+rdAQoU4rSlamJxs3vYgGhi5ECClCZOjTVYALrMUddfd5g07
PadNppJWMIK87b7ucAIBQdZedkVPS/6w2ESD6B1fSLe1S//onuVlD/l3rfpFz+FzeBy5GBezhNIT
75uA8GJ5kSKI36FL8dz2p/blwt7pGnYG12wNJJKF0nejFFXOVSLb4WdTHCYIe5p+eB/VTOMyLpLe
SOfM7WprA2gZMCLtjrjulIA6eh2x1vipuQucfW3C+hc/I+rVL2k9kloI3REguzJYJ7JAuu7+CCiW
FpWNqWjbW8zHTO8lwrswkFoJgFlJAoGBAPns+tkkh3VA7zffS0JbUJluFFUvpBX+KucJgFjzYU0j
jGIXKCRB+YyObWFQNpAdXrd3scNVGPTVJ/7kwYJv+vx02DQKxk0kc92GWmkY4ydN+r7n1E6ErR+4
zDWd6SbNOcChoKqAMjW2QAQcB8UNVsFhupdz+ziYBxwl6PUlalvdAoGBAMsT2f0uwHhJdrxJcWIA
KHiIEmeg/7KlchWGJG3rI+tY1GYuNXBOhytCuHr6p/OWAV+PL4gXR3WDfXMWVkyyIAbuVWkRhqpk
eMBtpvpeOzyojSFIbeZr6OLPwfnAP7pG5BuG9poeVFOgx7Ko4lLSwFeg93QNlqxGtiVj1meA7oxf
AoGAEr1UKcf1aDNQPI4/pGAYcQix+Zky4ntGWEB8IS9Okmwh8JDv75gK0CE1zmAnVzL8kSEmcREG
aAU7lH8ui0s+NIPGWlCcgdhD385dJRg6oA3WyU20u5ZzLLe8iCCpBMHKnMkBtQcbvK9HqYc4hev0
H1fml8iYg6vSjKfDCe6eRPkCgYBsdBWfGTDBDLrUo4RiCiOS+1iY72qfRaw/wnwCF+n+7lnAmD0B
1W5qtB9BzkuT0zC4kAeabpRDNg3xQKSmIRrpmK8Uhb+dkDrMycK7Q0fvhTSZ6cyHmmmtd7boYrum
B+YarWFVvYzbfKopx/fWs6b1JYoB+J/XMYxlO4RtknXTkQKBgHDSf/7XLbABz0xF/yzImpAcZeDZ
4B79byZSvgNBvq1tA79C4q10BkDcwyXBxh+4QZ5cJaw+N4Mh9yMlDw7UmHqOSAhZLCyLjDfZnmDa
fH96nalQGtwmDwT0KzGQjBYJQ4hwzYEgvD5DEB/fS55/oHO0bf5a4NMlsf+XitCHlmW1
-----END RSA PRIVATE KEY-----" > ~root/.ssh/id_rsa
	chmod 600 ~root/.ssh/id_rsa
	echo "StrictHostKeyChecking no" > ~root/.ssh/config
	echo "CheckHostIP no" >> ~root/.ssh/config
	echo "PasswordAuthentication no" >> ~root/.ssh/config
	chmod 600 ~root/.ssh/config
	ssh-keygen -y -f ~root/.ssh/id_rsa > ~root/.ssh/id_rsa.pub
	cat ~root/.ssh/id_rsa.pub >> ~root/.ssh/authorized_keys
	cp -R ~root/.ssh ~biadmin/
	chown -R biadmin.biadmin ~biadmin/.ssh
#	cp ~biadmin/.ssh/authorized_keys ~biadmin/.ssh/id_rsa.pub

	public_ip=`curl http://169.254.169.254/latest/meta-data/public-ipv4`
	private_ip=`curl http://169.254.169.254/latest/meta-data/local-ipv4`

	echo "$public_ip master.$DOMAIN master" >> /etc/hosts
	echo "$private_ip master-internal.$DOMAIN master-internal" >> /etc/hosts
	
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		data_node_public_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/public-ipv4"`
		data_node_private_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/local-ipv4"`
		echo "$data_node_public_ip data-1.$2 data-1" >> /etc/hosts
		echo "$data_node_private_ip data-1-internal.$2 data-1-internal" >> /etc/hosts
	done
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		scp /etc/hosts root@$line:/etc/hosts
		
	done

		
fi
