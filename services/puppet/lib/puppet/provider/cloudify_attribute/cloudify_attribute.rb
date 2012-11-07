require 'net/http'
require 'json'
require ::File.expand_path("../../helpers/cloudify.rb", ::File.dirname(__FILE__))

Puppet::Type.type(:cloudify_attribute).provide(:cloudify_attribute) do
    desc "Set cloudify attribute using REST interface"

    def create
        request = Net::HTTP::Post.new ::Cloudify.resource_url(resource)
        request.body = {resource[:name] => resource[:value]}.to_json
        request.content_type = 'application/json'
        ::Cloudify.cloudify_rest do |http| 
            http.request(request)
        end
    end

    def destroy
        ::Cloudify.cloudify_rest do |http|
            http.delete("#{::Cloudify.resource_url(resource)}/#{resource[:name]}")
        end
    end

    def exists?
        #TODO: set up a check to handle `destroy` well
        false
    end
end