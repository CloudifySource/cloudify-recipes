include_recipe "travel::common"
include_recipe "mysql::server"

db_conn = {:host => "localhost", :username => 'root', :password => node['mysql']['server_root_password']}

mysql_database "travel" do
  connection (db_conn)
end
mysql_database_user node["travel"]["db_user"] do
  connection (db_conn)
  database_name node["travel"]["db_name"]
  password node["travel"]["db_pass"]
  host "%"
  action [:create, :grant]
end