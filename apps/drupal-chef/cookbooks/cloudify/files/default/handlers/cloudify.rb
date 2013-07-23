module Cloudify
  module ChefHandlers
    class AttributesDumpHandler < ::Chef::Handler
      include ::Cloudify::REST

      def report
        cloudify_rest(:post,
                      :instance,
                      node["cloudify"]["application_name"],
                      node["cloudify"]["service_name"],
                      node["cloudify"]["instance_id"],
                      "chef_node_attributes",
                      node.to_json
                     )

        cloudify_rest(:post,
                      :instance,
                      node["cloudify"]["application_name"],
                      node["cloudify"]["service_name"],
                      node["cloudify"]["instance_id"],
                      "chef_run_status",
                      success? ? 'ok' : 'failed'
                     )

        cloudify_rest(:post, 
                      :instance,
                      node["cloudify"]["application_name"],
                      node["cloudify"]["service_name"],
                      node["cloudify"]["instance_id"],
                      "chef_updated_resources",
                      ::Chef::JSONCompat.to_json(updated_resources)
                     )
      end
    end
  end
end

