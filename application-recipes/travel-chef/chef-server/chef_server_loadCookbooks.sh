#! /bin/bash -e
cd ~

#TODO: move these into chef recipe so that the script is cross platform
export DEBIAN_FRONTEND=noninteractive
export DEBIAN_PRIORITY=critical
APT_GET="sudo apt-get -y"

$APT_GET install ruby-mime-types git

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

git clone https://github.com/CloudifySource/cloudify-recipes.git
ln -s $HOME/cloudify-recipes/application-recipes/travel-chef/cookbooks $HOME/cookbooks

knife cookbook upload -a
for role in $HOME/cloudify-recipes/travel-chef/roles/*.rb; do
    knife role from file $role
done
