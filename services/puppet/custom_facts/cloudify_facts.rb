#Load cloudify attributes into puppet
require 'net/http'
require 'uri'
require 'json'

@metadata = JSON.parse open("/opt/cloudify/metadata.json").read()
if @metadata.nil? or @metadata.empty?
    raise Exception.new """Cloudify metadata was not found in /opt/cloudify/metadata.json
                           It should have been written during instance installation."""
end
@rest_port = 8100

def get_json(resource)
    res = Net::HTTP.start(@metadata['managementIP'], @rest_port) {|http| http.get(resource)}
    JSON.parse(res.body)
end

attributes = {}
["/attributes/globals/",
 "/attributes/applications/#{@metadata['application']}",
 "/attributes/services/#{@metadata['application']}/#{@metadata['service']}/",
 "/attributes/instances/#{@metadata['application']}/#{@metadata['service']}/#{@metadata['instanceID']}/"
].each do |resource|
    attributes.merge! get_json(resource)
end

class Hash
    def flatten_to_hash(current_prefix="", separator=".")
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
attributes = attributes.flatten_to_hash

attributes.each do |key, value|
    Facter.add(key) do
      setcode do
        value.to_s
      end
    end
end