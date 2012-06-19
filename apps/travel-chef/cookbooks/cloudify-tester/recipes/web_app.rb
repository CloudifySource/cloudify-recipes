include_recipe "cloudify-tester::default"

remote_directory ::File.join(node['cloudify-tester']['cucumber_dir'],"web_app") do
  source "web_app"
  files_owner "ubuntu"
  files_group "ubuntu"
  files_mode "0777"
  owner "ubuntu"
  group "ubuntu"
  mode "0777"
end
