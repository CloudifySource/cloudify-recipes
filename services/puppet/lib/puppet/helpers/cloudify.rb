require 'net/http'
require 'json'

#ruby helper functions for interacting with the cloudify management machine
module Cloudify
    #read and return cloudifify metadata for this node
    def self.metadata
        if not @metadata
            @metadata = JSON.parse open("/opt/cloudify/metadata.json").read()
            if @metadata.nil? or @metadata.empty?
                raise RuntimeError.new """
                    Cloudify metadata was not found in /opt/cloudify/metadata.json
                    It should have been written during instance installation."""
            end
        end
        @metadata
    end

    #a wrapper for et::HTTP.start that yields a connection to REST interface
    def self.cloudify_rest
        result = Net::HTTP.start(::Cloudify.metadata['managementIP'],
                                 ::Cloudify.metadata["REST_port"]
                            ) do |http|
            yield http
        end

        case result
        when Net::HTTPSuccess, Net::HTTPRedirection
          # OK
        else
          raise RuntimeError.new "The REST request failed with code #{result.value}"
        end
    end

    #returns a resource url to be used in a REST request
    def self.resource_url(resource)
        application = resource[:application] || ::Cloudify.metadata['application']
        service     = resource[:service]     || ::Cloudify.metadata['service']
        instance_id = resource[:instance_id] || ::Cloudify.metadata['instanceID']

        case resource[:type]
            when "global"      then "/attributes/globals"
            when "application" then "/attributes/applications/#{application}"
            when "service"     then "/attributes/services/#{application}/#{service}"
            when "instance"    then "/attributes/instances/#{application}/#{service}/#{instance_id}"
            else raise ArgumentError.new """Invalid attribute type '#{resource[:type]}'.
                                            Use one of: global/application/service/instance"""
        end
    end
end