maintainer       "fewbytes for gigaspaces"
maintainer_email "yoni@fewbytes.com"
license          "All rights reserved"
description      "Installs/Configures cloudify-tester"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.0.1"

%w{ jruby }.each do |cb|
  depends cb
end
