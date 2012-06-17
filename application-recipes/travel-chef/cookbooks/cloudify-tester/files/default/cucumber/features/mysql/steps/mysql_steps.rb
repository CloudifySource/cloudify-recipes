require 'rubygems'

AVERAGING_TIMEFRAME = 2

Before do
  @username = 'root'
  @password = ''
  @database = nil
end

#below is a hack to fetch a single integer value under both jruby and MRI
if RUBY_PLATFORM == "java"
  require "jdbc/mysql"

  def query_integer(query)
      Java::com.mysql.jdbc.Driver
      userurl = "jdbc:mysql://#{@host}/#{@database}"
      @connSelect = java.sql.DriverManager.get_connection(userurl, @username, @password)
      @stmtSelect = @connSelect.create_statement
    
      res = @stmtSelect.execute_query(query)
      res.next
      res.getString(1).to_i
  end
else #assume MRI
  require 'mysql'

  def query_integer(query)
    Mysql.connect(@host, @username, @password, @database).query(query).fetch_row[0].to_i
  end 
end

#Helper functions
def get_process_count()
  query_integer('select count(1) from information_schema.processlist;')
end

def get_table_count(table_like)
  query_integer("select count(1) from information_schema.tables" +
                " where table_schema = '#{@database}' and table_name like '#{table_like}';")
end

def get_global_status_sum(variables)
  in_string = variables.map{ |str| "'#{str}'" }.join(", ")
  query_integer("select sum(variable_value) as value from information_schema.global_status" +
          " where variable_name in(#{in_string});")
end

def count_global_status_per_second(variables)
  start_count = get_global_status_sum(variables) 
  sleep AVERAGING_TIMEFRAME
  end_count = get_global_status_sum(variables) 
  (end_count-start_count)/AVERAGING_TIMEFRAME
end

# Step definitions for testing the state of the MySQL server
Given /I have a MySQL server on (.+)$/ do |host|
  @host = host
end

And /I use the username (.+)$/ do |username|
  @username = username
end

And /I use the password (.+)$/ do |password|
  @password = password
end

And /I use the database (.+)$/ do |database|
  @database = database
end

#schema tests:
Then /^it should have the table ([^\'\"]+)$/ do |table|
  get_table_count(table).should == 1
end

Then /^it should not have the table ([^\'\"]+)$/ do |table|
  get_table_count(table).should == 0
end

#statistical tests:
Then /it should have less than (\d+) processes$/ do |processes|
  get_process_count().should < processes.to_i
end

Then /it should have at least (\d+) processes$/ do |processes|
  get_process_count().should >= processes.to_i
end

Then /it should have less than (\d+) connections$/ do |connections|
  get_global_status_sum(['Connections']).should < connections.to_i
end

Then /it should have at least (\d+) connections$/ do |connections|
  get_global_status_sum(['Connections']).should >= connections.to_i
end

Then /it should have less than (\d+) threads running$/ do |threads|
  get_global_status_sum(['Threads_running']).should < threads.to_i
end

Then /it should have at least (\d+) threads running$/ do |threads|
  get_global_status_sum(['Threads_running']).should >= threads.to_i
end

Then /it should have less than (\d+) threads connected$/ do |threads|
  get_global_status_sum(['Threads_connected']).should < threads.to_i
end

Then /it should have at least (\d+) threads connected$/ do |threads|
  get_global_status_sum(['Threads_connected']).should >= threads.to_i
end

Then /it should have at least (\d+) queries cached$/ do |cached_queries|
  get_global_status_sum(['Qcache_queries_in_cache']).should >= cached_queries.to_i
end

Then /it should have at least (\d+) queries cached$/ do |cached_queries|
  get_global_status_sum(['Qcache_queries_in_cache']).should >= cached_queries.to_i
end

Then /it should have less than (\d+) update queries per second$/ do |queries|
  count_global_status_per_second(['Com_update']).should < queries.to_i
end

Then /it should have at least (\d+) update queries per second$/ do |queries|
  count_global_status_per_second(['Com_update']).should >= queries.to_i
end

Then /it should have less than (\d+) insert queries per second$/ do |queries|
  count_global_status_per_second(['Com_insert']).should < queries.to_i
end

Then /it should have at least (\d+) insert queries per second$/ do |queries|
  count_global_status_per_second(['Com_insert']).should >= queries.to_i
end

Then /it should have less than (\d+) delete queries per second$/ do |queries|
  count_global_status_per_second(['Com_delete']).should < queries.to_i
end

Then /it should have at least (\d+) delete queries per second$/ do |queries|
  count_global_status_per_second(['Com_delete']).should >= queries.to_i
end

Then /it should have less than (\d+) selects queries per second$/ do |queries|
  count_global_status_per_second(['Com_select']).should < queries.to_i
end

Then /it should have at least (\d+) selects queries per second$/ do |queries|
  count_global_status_per_second(['Com_select']).should >= queries.to_i
end

Then /it should have less than (\d+) queries per second$/ do |queries|
  count_global_status_per_second(['Com_update', 'Com_select', 'Com_delete', 'Com_insert']).should < queries.to_i
end

Then /it should have at least (\d+) queries per second$/ do |queries|
  count_global_status_per_second(['Com_update', 'Com_select', 'Com_delete', 'Com_insert']).should >= queries.to_i
end

Then /it should have less than (\d+) cache hits per second$/ do |queries|
  count_global_status_per_second(['Qcache_hits']).should < queries.to_i
end

Then /it should have at least (\d+) cache hits per second$/ do |queries|
  count_global_status_per_second(['Qcache_hits']).should >= queries.to_i
end

Then /it should have less than (\d+) slow queries per second$/ do |queries|
  count_global_status_per_second(['Slow_queries']).should < queries.to_i
end

Then /it should have at least (\d+) slow queries per second$/ do |queries|
  count_global_status_per_second(['Slow_queries']).should >= queries.to_i
end

Then /it should have less than (\d+) questions per second$/ do |queries|
  count_global_status_per_second(['Questions']).should < queries.to_i
end

Then /it should have at least (\d+) questions per second$/ do |queries|
  count_global_status_per_second(['Questions']).should >= queries.to_i
end
