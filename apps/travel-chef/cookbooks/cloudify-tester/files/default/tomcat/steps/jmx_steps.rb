require 'rubygems'
require 'jmx4r'

def massage_attribute(att)
  #jmx4r uses underscore_separation instead of Java's camelCase
  att.gsub(/[A-Z]/, '_\0').downcase
end

Given /^I have JMX exposed locally on port (\d+)$/ do |port|
  JMX::MBean.establish_connection :host => "localhost", :port => port
end

Given /^I have JMX exposed on port (\d+) of (.*?) $/ do |port, host|
  JMX::MBean.establish_connection :host => host, :port => port
end

When /^I examine the object "(.*?)"$/ do |jmx_name|
  @jmx_obj = JMX::MBean.find_by_name jmx_name
end

Then /^the attribute "(.*?)" is equal to (.*?)$/ do |attribute, expected_value|
  @jmx_obj.send(massage_attribute(attribute)).to_s.should == expected_value
end

Then /^the attribute "(.*?)" is at least (.*?)$/ do |attribute, expected_value|
  @jmx_obj.send(massage_attribute(attribute)).to_f.should >= expected_value.to_f
end

Then /^the attribute "(.*?)" is less than (.*?)$/ do |attribute, expected_value|
  @jmx_obj.send(massage_attribute(attribute)).to_f.should < expected_value.to_f
end

Then /^the attribute "(.*?)" is between (.*?) and (.*?)$/ do |attribute, min_value, max_value|
  @jmx_obj.send(massage_attribute(attribute)).to_f.should be_between(min_value.to_f, max_value.to_f)
end
