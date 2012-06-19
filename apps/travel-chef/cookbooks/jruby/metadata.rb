maintainer       "Jorge Falcão"
maintainer_email "falcao@intelie.com.br"
license          "Apache 2.0"
description      "Installs/Configures jruby"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.2.2"

depends "java"
depends "install_from"

supports "ubuntu"
supports "debian"
