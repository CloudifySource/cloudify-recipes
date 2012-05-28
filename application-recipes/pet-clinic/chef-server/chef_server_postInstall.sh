#! /bin/bash -e
cd ~
export DEBIAN_FRONTEND=noninteractive
export DEBIAN_PRIORITY=critical
APT_GET="sudo apt-get -y"

$APT_GET install ruby-mime-types

mkdir .chef
cat - > .chef/knife.rb <<EOF
log_level                :info
log_location             STDOUT
node_name                'chef-webui'
client_key               '$HOME/.chef/chef-webui.pem'
validation_client_name   'chef-validator'
validation_key           '$HOME/.chef/chef-validator.pem'
chef_server_url          'http://localhost:4000'  
cache_type               'BasicFile'
cache_options( :path => '$HOME/.chef/checksums' )
cookbook_path [ '$HOME/cookbooks' ]
EOF

sudo cp /etc/chef/webui.pem .chef/chef-webui.pem
sudo chown `whoami` .chef/chef-webui.pem

wget http://public.fewbytes.com/cloudify-pet-clinic-cookbooks.tar.gz
wget http://public.fewbytes.com/cloudify-pet-clinic-roles.tar.gz
tar -xzf cloudify-pet-clinic-cookbooks.tar.gz
tar -xzf cloudify-pet-clinic-roles.tar.gz

knife cookbook upload -a
for role in roles/*.rb; do
    knife role from file $role
done
