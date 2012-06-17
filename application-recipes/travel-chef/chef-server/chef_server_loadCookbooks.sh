#! /bin/bash -e

#TODO: move these into chef recipe so that the script is cross platform
export DEBIAN_FRONTEND=noninteractive
export DEBIAN_PRIORITY=critical
if [[ `whoami` != root ]]; then
    SUDO=sudo
fi
APT_GET="$SUDO apt-get -y"

ruby -r mime/types -e true || $APT_GET install ruby-mime-types
which git > /dev/null || $APT_GET install git

[[ -d "$HOME/.chef" ]] || mkdir "$HOME/.chef"
cat - > ~/.chef/knife.rb <<EOF
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

$SUDO cp /etc/chef/webui.pem ~/.chef/chef-webui.pem
$SUDO chown `whoami` ~/.chef/chef-webui.pem

if [[ -d $HOME/cloudify-recipes/.git ]]; then
    cd $HOME/cloudify-recipes; git pull origin master; cd -
else
    git clone https://github.com/CloudifySource/cloudify-recipes.git $HOME/cloudify-recipes
fi

[[ -r $HOME/cookbooks ]] || ln -s $HOME/cloudify-recipes/application-recipes/travel-chef/cookbooks $HOME/cookbooks

knife cookbook upload -a
for role in $HOME/cloudify-recipes/application-recipes/travel-chef/roles/*.rb; do
    knife role from file $role
done
