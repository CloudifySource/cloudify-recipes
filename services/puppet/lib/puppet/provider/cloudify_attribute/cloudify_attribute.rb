require 'net/http'
require 'uri'
require 'json'

Puppet::Type.type(:cloudify_attribute).provide(:cloudify_attribute) do
    desc "Cloudify attribute setter"

    def cloudify_rest
        #TODO: move these to a constructor?
        metadata = JSON.parse open("/opt/cloudify/metadata.json").read()
        if metadata.nil? or metadata.empty?
            raise Exception.new """Cloudify metadata was not found in /opt/cloudify/metadata.json
                                   It should have been written during instance installation."""
        end
        rest_port = 8100

        Net::HTTP.start(metadata['managementIP'], rest_port) do |http| 
            yield http
        end
    end

    def create
        cloudify_rest do |http| 
            #TODO: add globals/application/service attributes as well
            post_data = {resource[:name] => resource[:value]}.to_json
            http.post("/attributes/instances/#{resource[:application]}/#{resource[:service]}/#{resource[:instance_id]}",
                           post_data)
        end
    end

    def destroy
        cloudify_rest do |http|
            #TODO: add globals/application/service attributes as well
            http.delete("/attributes/instances/#{resource[:application]}/#{resource[:service]}/#{resource[:instance_id]}/#{resource[:name]}")
        end
    end

    def exists?
        false
    end
end