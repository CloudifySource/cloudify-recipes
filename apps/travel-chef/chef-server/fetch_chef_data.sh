#! /bin/bash
#This command has been split off from chef_server_loadCookbooks,
#to allow easily fetching the cookbooks and roles from another repo.

REPO_DIR=$1/cloudify-recipes
COOKBOOK_DIR=$1/cookbooks
ROLE_DIR=$1/roles

if [[ -d $REPO_DIR/.git ]]; then
    cd $REPO_DIR; git pull origin master; cd -
else #first run
    git clone https://github.com/CloudifySource/cloudify-recipes.git $REPO_DIR
fi

[[ -r $HOME/cookbooks ]] || ln -s $REPO_DIR/apps/travel-chef/cookbooks $COOKBOOK_DIR
[[ -r $HOME/roles ]] || ln -s $REPO_DIR/apps/travel-chef/roles $ROLE_DIR
