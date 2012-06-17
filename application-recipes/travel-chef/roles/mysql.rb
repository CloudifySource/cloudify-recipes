name "mysql"
description "mysql master server"
run_list "recipe[travel::mysql]", "recipe[cloudify-tester::mysql]"
