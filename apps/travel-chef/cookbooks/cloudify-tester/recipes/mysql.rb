include_recipe "cloudify-tester::default"

run_cucumber "mysql" do
  feature "localhost"
end
