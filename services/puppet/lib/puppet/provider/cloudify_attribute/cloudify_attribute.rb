require 'net/http'
require 'uri'
require 'rubygems'
require 'json'

Puppet::Type.type(:cloudify_attribute).provide(:cloudify_attribute) do
    desc "Cloudify attribute setter"

    def initialize(args)
        #TODO: make and use a cloudify_rest library instead
        @metadata = JSON.parse open("/opt/cloudify/metadata.json").read()
        if @metadata.nil? or @metadata.empty?
            raise RuntimeError.new """
                Cloudify metadata was not found in /opt/cloudify/metadata.json
                It should have been written during instance installation."""
        end
        @rest_port = 8100
        super(args)
    end

    def cloudify_rest
        result = Net::HTTP.start(@metadata['managementIP'], @rest_port) do |http| 
            yield http
        end

        case result
        when Net::HTTPSuccess, Net::HTTPRedirection
          # OK
        else
          raise RuntimeError.new "The REST request failed with code #{result.value}"
        end
    end

    def resource_url
        application = resource[:application] || @metadata['application']
        service     = resource[:service]     || @metadata['service']
        instance_id = resource[:instance_id] || @metadata['instanceID']

        case resource[:type]
            when "global"      then "/attributes/globals"
            when "application" then "/attributes/applications/#{application}"
            when "service"     then "/attributes/services/#{application}/#{service}"
            when "instance"    then "/attributes/instances/#{application}/#{service}/#{instance_id}"
            else raise ArgumentError.new """Invalid attribute type '#{resource[:type]}'.
                                            Use one of: global/application/service/instance"""
        end
    end

    def create
        request = Net::HTTP::Post.new(resource_url)
        request.body = {resource[:name] => resource[:value]}.to_json
        request.content_type = 'application/json'
        cloudify_rest do |http| 
            http.request(request)
        end
    end

    def destroy
        cloudify_rest do |http|
            http.delete("#{resource_url}/#{resource[:name]}")
        end
    end

    def exists?
        #I wonder what I should do here to handle `destroy`s well
        false
    end
end