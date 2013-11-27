include Cloudify::REST

def whyrun_supported?
    true
end

action :set do
    raise RuntimeError, "new_resource.value missing" if new_resource.value.nil?
    converge_by("Setting Cloudify attribute #{new_resource.name}=#{new_resource.value} for #{new_resource.scope} #{new_resource.identity}") do
        cloudify_rest(:post,
                      new_resource.scope, 
                      new_resource.application || node['cloudify']['application_name'],
                      new_resource.service || node['cloudify']['service_name'],
                      new_resource.instance || node['cloudify']['instance_id'],
                      new_resource.key, new_resource.value)
    end
end

action :unset do
    converge_by("Unset cloudify attribute #{new_resource.name} for #{new_resource.scope} #{new_resource.identity}") do
        cloudify_rest(:delete,
                      new_resource.scope, 
                      new_resource.application || node['cloudify']['application_name'],
                      new_resource.service || node['cloudify']['service_name'],
                      new_resource.instance || node['cloudify']['instance_id'],
                      new_resource.key)
    end
end
