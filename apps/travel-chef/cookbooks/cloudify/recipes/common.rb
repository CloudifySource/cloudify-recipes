# Just a place to put various actions we'd like to put on all cloudify-chef instances

case node["platform"]
when "debian", "ubuntu"
    include_recipe "apt"
end
