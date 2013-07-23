name             'drupal'
maintainer       'tamirko'
maintainer_email 'tamir@gigaspaces,com'
license          'All rights reserved'
description      'Installs/Configures drupal'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          '0.1.0'

depends "apache2"
depends "mysql"
depends "php"
depends "openssl"
depends "database"
depends "ark"
