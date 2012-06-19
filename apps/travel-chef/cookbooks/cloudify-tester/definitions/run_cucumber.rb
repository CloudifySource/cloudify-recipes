define :run_cucumber, :feature => nil do
  bash "run cucumber feature /#{params[:name]}/#{params[:feature]}"
    user "root"
    cwd node['cloudify-tester']['cucumber_dir']
    code "jruby -S cucumber features/#{params[:name]}/#{params[:feature]}.feature"
    ignore_failure true
  end
end
