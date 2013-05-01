# Chef server installation

Installs the open source Chef server using Chef-Solo. 

## Supported platforms

chef-server does not support 32bit OS. Make sure you assign a proper image in your recipe. The default image in this recipe is not defined in default cloud recipes!
Currently there is a bug in Opscode's Omnitruck download API when using Amazon Linux.

> *Important*: Currently the chef recipes have only been tested on an Ubuntu environment on Amazon EC2. Please make sure to use the [EC2 ubuntu cloud driver](https://github.com/CloudifySource/cloudify-cloud-drivers/tree/master/ec2-ubuntu) when installing services or applications that are based on this recipe


