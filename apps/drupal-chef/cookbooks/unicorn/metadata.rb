name             "unicorn"
maintainer       "Opscode, Inc"
maintainer_email "cookbooks@opscode.com"
license          "Apache 2.0"
description      "Installs/Configures unicorn"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "1.3.0"
recipe "unicorn", "Installs unicorn rubygem"
