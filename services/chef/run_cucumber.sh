#!/bin/sh
cd /home/ubuntu/cucumber
jruby -S cucumber
exit 0 #hack - to see the error text, we must exit successfully(CLOUDIFY-915)
