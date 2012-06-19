include_recipe "cloudify-tester::default"

run_cucumber "web_app"
  feature "localhost"
end
