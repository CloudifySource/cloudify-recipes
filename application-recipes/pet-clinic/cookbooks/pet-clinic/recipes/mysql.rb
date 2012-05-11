include_recipe "pet-clinic::common"
include_recipe "mysql::server"

mysql_database "petclinic" do
  connection ({:host => "localhost", :username => 'root', :password => node['mysql']['server_root_password']})
end
mysql_database_user "petclinic" do
  connection ({:host => "localhost", :username => 'root', :password => node['mysql']['server_root_password']})
  database_name "petclinic"
  password "test"
end


