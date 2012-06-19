define :run_cucumber, :feature => nil do
  execute "run cucumber feature /#{params[:name]}/#{params[:feature]}" do
    user "root"
    cwd node['cloudify-tester']['cucumber_dir']
    command "jruby -S cucumber features/#{params[:name]}/#{params[:feature]}.feature"
    ignore_failure true
  end 
end
