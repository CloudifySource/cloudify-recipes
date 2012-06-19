include_recipe "cloudify-tester::default"

run_cucumber "tomcat"
  feature "localhost"
end
