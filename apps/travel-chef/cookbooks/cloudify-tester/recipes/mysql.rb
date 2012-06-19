include_recipe "cloudify-tester::default"

run_cucumber "mysql"
  feature "localhost"
end
