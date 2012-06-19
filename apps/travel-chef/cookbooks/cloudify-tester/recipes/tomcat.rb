include_recipe "cloudify-tester::default"

run_cucumber "tomcat" do
  feature "localhost"
end
