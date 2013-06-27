attribute :key, :name_attribute => true
attribute :value, :default => nil
attribute :scope, :default => :application, :equal_to => [:global, :application, :service, :instance]
attribute :application, :default => nil, :kind_of => String
attribute :service, :default => nil, :kind_of => String
attribute :instance, :default => nil, :kind_of => String

actions :set, :unset
default_action :set
