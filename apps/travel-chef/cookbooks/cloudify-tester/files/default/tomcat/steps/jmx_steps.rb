require 'rubygems'
require 'jmx4r'

Given /^I have JMX exposed locally on port (\d+)$/ do |port|
  JMX::MBean.establish_connection :host => "localhost", :port => port
end

Given /^I have JMX exposed on port (\d+) of (.*?) $/ do |port, host|
  JMX::MBean.establish_connection :host => host, :port => port
end

When /^I examine the object "(.*?)"$/ do |jmx_name|
  @jmx_obj = JMX::MBean.find_by_name jmx_name
end

# to list attributes of an object: JMX::MBean.pretty_print("java.lang:type=Memory")

Then /^the attribute "(.*?)" is equal to (.*?)$/ do |attribute, expected_value|
  @jmx_obj.send(attribute.snake_case).to_s.should == expected_value
end

Then /^the attribute "(.*?)" is at least ([0-9]+)$/ do |attribute, expected_value|
  @jmx_obj.send(attribute.snake_case).to_f.should >= expected_value.to_f
end

Then /^the attribute "(.*?)" is less than ([0-9]+)$/ do |attribute, expected_value|
  @jmx_obj.send(attribute.snake_case).to_f.should < expected_value.to_f
end

Then /^the attribute "(.*?)" is between ([0-9]+) and (.*?)$/ do |attribute, min_value, max_value|
  @jmx_obj.send(attribute.snake_case).to_f.should be_between(min_value.to_f, max_value.to_f)
end

Then /^the current heap memory usage is less than ([0-9\.]+) of the max allowed$/ do |multiplier|
  mem_hash = @jmx_obj.send("HeapMemoryUsage".snake_case)
  mem_hash["used"].should be < multiplier.to_f * mem_hash["max"]
end
