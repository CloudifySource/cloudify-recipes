require 'net/http'
require 'uri'

# Ruby helper functions for interacting with the cloudify management machine
# At some point this should be rewritten as a more re-usable library
module Cloudify
  module REST
    #a wrapper for et::HTTP.start that yields a connection to REST interface
    def cloudify_rest(method, scope, application, service, instance, key, value=nil)
        cloudify_uri = URI.parse(node['cloudify']['management_rest_url'])
        cloudify_rest_uri = cloudify_resource_uri(scope, application, service, instance)
        request = case method
        when :post
            r = ::Net::HTTP::Post.new(cloudify_rest_uri)
            r.body = ::Chef::JSONCompat.to_json({key => value}) if value
            r
        when :delete
            ::Net::HTTP::Delete.new(cloudify_rest_uri + "/" + key)
        when :get
            ::Net::HTTP::Get.new(cloudify_rest_uri)
        else
            raise RuntimeError, "Method #{method} not implemented for Cloudify REST"
        end

        request.content_type = 'application/json'     

        response = Net::HTTP.start(cloudify_uri.host, cloudify_uri.port) do |http|
            http.request(request)
        end
        
        unless response.code == '200'
          raise RuntimeError.new "The REST request failed with code #{response.code}"
        end
    end

    #returns a resource url to be used in a REST request
    def cloudify_resource_uri(scope, application, service, instance_id)

        case scope
            when :global      then "/attributes/globals"
            when :application then "/attributes/applications/#{application}"
            when :service     then "/attributes/services/#{application}/#{service}"
            when :instance    then "/attributes/instances/#{application}/#{service}/#{instance_id}"
            else raise ArgumentError.new """Invalid attribute type '#{scope}'.
                                            Use one of: global/application/service/instance"""
        end
    end

    def cloudify_get_attribute(scope, attribute, opts={})
        application = opts.fetch("application", node['cloudify']['application_name'])
        service = opts.fetch("service", node['cloudify']['service_name'])
        instance = opts.fetch("instance", node['cloudify']['instance_id'])
        ::Chef::JSONCompat.from_json(cloudify_rest(:get, scope, application, service, instance, attribute).body)[attribute]
    end
  end
end

class Chef::Recipe; include ::Cloudify::REST; end
