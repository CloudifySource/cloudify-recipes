require 'json'
require ::File.expand_path("../../helpers/cloudify.rb", ::File.dirname(__FILE__))

Puppet::Parser::Functions.newfunction(
    :get_cloudify_attribute,
    :type => :rvalue,
    :doc => "Read cloudify attribute using REST interface"
) do |args|
    attribute_name = args[0] || (raise ArgumentError.new "No attribute name provided")
    resource = {:type => args[1] || "global",
                :application => args[2],
                :service => args[3],
                :instance_id => args[4]
               }

    response = nil
    ::Cloudify.cloudify_rest do |http| 
        response = http.get("#{::Cloudify.resource_url(resource)}/#{attribute_name}")
    end
    JSON.parse(response.body)[attribute_name]
end