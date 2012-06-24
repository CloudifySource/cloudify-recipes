#! /bin/bash
#This command has been split off from chef_server_loadCookbooks to allow testing the code from a fork.
TARGET_DIR=$1 
git clone -b dev https://github.com/yoniYalovitsky/cloudify-recipes.git $TARGET_DIR 
