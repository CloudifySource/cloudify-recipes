require 'rubygems'
require 'jmx4r'

Given /^I have JMX exposed locally on port (\d+)$/ do |port|
  JMX::MBean.establish_connection :host => "localhost", :port => port
end

Given /^I have JMX exposed on port (\d+) of '(.*?)' $/ do |port, host|
  JMX::MBean.establish_connection :host => host, :port => port
end

Given /^I have the object '(.*?)'$/ do |jmx_name|
  @jmx_obj = JMX::MBean.find_by_name jmx_name
end

Then /^it should have attribute '(.*?)' with value '(.*?)'$/ do |attribute, expected_value|
  #jmx4r uses underscore_separation instead of Java's camelCase
  massaged_attribute = attribute.gsub(/[A-Z]/, '_\0').downcase
  
  @jmx_obj.send(massaged_attribute).should == expected_value
end

