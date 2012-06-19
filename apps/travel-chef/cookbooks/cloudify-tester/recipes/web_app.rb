include_recipe "cloudify-tester::default"

run_cucumber "web_app" do
  feature "localhost"
end
