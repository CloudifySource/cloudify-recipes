CLOUDIFY_METADATA_FILE = "/opt/cloudify/metadata.json"

provides "cloudify"

cloudify Mash.new

if ENV.include? 'USM_INSTANCE_ID'
	ENV.select{|k, v| k.starts_with? "USM_"}.each {|k,v| cloudify[k.sub(/USM_/,'').downcase.to_sym] = v}
end

if File.exists? "/opt/cloudify/metadata.json"
	File.open(CLOUDIFY_METADATA_FILE, 'r') do |f|
		cloudify.merge!(Yajl::Parser.new.parse(f))
	end
end