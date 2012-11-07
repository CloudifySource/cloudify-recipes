Puppet::Type.newtype(:cloudify_attribute) do
    @doc = "Set cloudify attributes"

    ensurable

    newparam(:name) do
        desc "Attribute name"
        isnamevar
    end
    newparam(:value) do
        desc "Attribute value"
    end
    newparam(:type) do
        desc "Attribute type"
        defaultto "global"
    end
    newparam(:application) do
        desc "Application name"
    end
    newparam(:service) do
        desc "Service name"
    end
    newparam(:instance_id) do
        desc "Instance id"
    end
end