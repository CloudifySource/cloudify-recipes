name             "gecode"
maintainer       "Opscode, Inc."
maintainer_email "cookbooks@opscode.com"
license          "Apache 2.0"
description      "Installs gecode"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "2.0.3"

%w{ debian ubuntu redhat centos scientific fedora mac_os_x }.each do |os|
  supports os
end

%w{ build-essential apt yum }.each do |cb|
  depends cb
end
