include_recipe "travel::common"
include_recipe "mysql::server"

mysql_database "travel" do
  connection ({:host => "localhost", :username => 'root', :password => node['mysql']['server_root_password']})
end
mysql_database_user "travel" do
  connection ({:host => "localhost", :username => 'root', :password => node['mysql']['server_root_password']})
  database_name "travel"
  password "test"
  host "%"
  action [:create, :grant]
end


