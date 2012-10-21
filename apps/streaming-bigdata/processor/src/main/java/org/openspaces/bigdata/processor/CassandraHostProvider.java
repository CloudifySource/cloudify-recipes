package org.openspaces.bigdata.processor;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnit;
import org.springframework.beans.factory.FactoryBean;

public class CassandraHostProvider implements FactoryBean<String> {
	
	private String host = "localhost"; 
		
	@PostConstruct
	public void getHostName(){
		
		Admin admin = new AdminFactory().createAdmin();
		ProcessingUnit cassandraPU = admin.getProcessingUnits().waitFor("big_data_app.cassandra", 180, TimeUnit.SECONDS);
		host = cassandraPU.getInstances().length != 0 ? cassandraPU.getInstances()[0].getMachine().getHostName() : "localhost";
	}

	@Override
	public String getObject() throws Exception {
		return host;
	}

	@Override
	public Class<String> getObjectType() {
		return String.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
