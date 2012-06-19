name "mysql"
description "mysql master server"
run_list "recipe[travel::mysql]", "recipe[cloudify-tester::mysql]"
default_attributes "mysql" => { "bind_address" => "0.0.0.0" }
