#Load cloudify attributes into puppet
require 'net/http'
require 'uri'
require 'json'

@metadata = JSON.parse open("/opt/cloudify/metadata.json").read()
if @metadata.nil? or @metadata.empty?
    raise Exception.new """Cloudify metadata was not found in /opt/cloudify/metadata.json
                           It should have been written during instance installation."""
end

def get_json(resource)
    res = Net::HTTP.start(@metadata['managementIP'], @metadata["REST_port"]) {|http| http.get(resource)}
    JSON.parse(res.body)
end

attributes = {}
{"global" => "/attributes/globals/",
 "application" => "/attributes/applications/#{@metadata['application']}",
 "service" => "/attributes/services/#{@metadata['application']}/#{@metadata['service']}/",
 "instance" => "/attributes/instances/#{@metadata['application']}/#{@metadata['service']}/#{@metadata['instanceID']}/"
}.each do |category, resource|
    attributes.merge!({category => get_json(resource)})
end
attributes.merge!("cloudify" => @metadata)

class Hash
    def flatten_to_hash(current_prefix="", separator="_")
        {}.tap do |hash|
            self.each do |key, value|
                if value.is_a?(Hash)
                    hash.merge!(value.flatten_to_hash(
                                    "#{current_prefix}#{key}#{separator}",
                                    separator))
                else
                    hash["#{current_prefix}#{key}"] = value
                end
            end
        end
    end
end

attributes.flatten_to_hash.each do |key, value|
    Facter.add(key) do
      setcode do
        value.to_s
      end
    end
end
