package org.openspaces.bigdata.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.StringUtils;

/**
 This class is a generic bean that discovers cloudify service instances running on the same application as this processing unit
 And injects it into a property so it can be used in other bean configurations.

 For example if service="cassandra" and outputProperty="cassandra.ip-addresses", then in any other bean the string "${cassandra.ip-addresses}"
 is replaced with a comma seperated list of cassandra instance ip addreses.
 */
public class ServiceDiscoveryPropertyPlaceHolderConfigurer extends PropertyPlaceholderConfigurer implements ClusterInfoAware , InitializingBean {

    private String service;
    private String outputProperty;
    private long timeoutSeconds = 60;
    private String[] ipAddresses;
    private ClusterInfo clusterInfo;
    private int minimumNumberOfInstances=1;

    public ServiceDiscoveryPropertyPlaceHolderConfigurer() {
        super.setIgnoreUnresolvablePlaceholders(true);
        super.setOrder(0); // medium precedence
    }

    @Required
    public void setService(String service) {
        this.service = service;
    }

    @Required
    public void setOutputProperty(String property) {
        this.outputProperty = property;
    }

    @Required
    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    @Required
    public void setMinimumNumberOfInstances(int minimumNumberOfInstances) {
        this.minimumNumberOfInstances = minimumNumberOfInstances;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final String application = getApplicationName();

        final ProcessingUnitInstance[] instances = waitForInstances(application, service, minimumNumberOfInstances, timeoutSeconds, TimeUnit.SECONDS);
        this.ipAddresses = extractIpAddresses(instances);
        if (logger.isDebugEnabled()) {
            logger.debug("Initialized " + getClass() + " with " + outputProperty+"="+ipAddresses);
        }
    }

    // Duplication from cloudify ServiceUtils.
    // to not include a dependency on the dsl jar.
    private String getApplicationName() {
        if (clusterInfo == null) {
        	throw new IllegalArgumentException("Could not parse PU name. Integrated Processing Units are not supported");
        }
    	final int index = clusterInfo.getName().lastIndexOf('.');
        if (index < 0) {
            throw new IllegalArgumentException("Could not parse PU name: " + clusterInfo.getName()
                    + " to read service and application names.");
        }

        return clusterInfo.getName().substring(0, index);
    }

    private static ProcessingUnitInstance[] waitForInstances(final String applicationName, final String serviceName, final int minimumNumberOfInstances, final long timeout, final TimeUnit timeunit) {
        final ProcessingUnit pu = waitForProcessingUnit(applicationName, serviceName, timeout, timeunit);
        pu.waitFor(minimumNumberOfInstances, timeout, timeunit);
        final ProcessingUnitInstance[] instances = pu.getInstances();
        if (instances.length < minimumNumberOfInstances) {
            throw new IllegalStateException("Could not discover " + minimumNumberOfInstances + " "+ serviceName + " instances in application " + applicationName);
        }
        return instances;
    }

    private static ProcessingUnit waitForProcessingUnit(final String applicationName, final String serviceName,  final long timeout, final TimeUnit timeunit) {
        final Admin admin = new AdminFactory().createAdmin();
        final String puName = applicationName + "." + serviceName;
        final ProcessingUnit pu = admin.getProcessingUnits().waitFor(puName, timeout, timeunit);
        if (pu == null) {
            throw new IllegalStateException("Could not discover service " + serviceName + " in application " + applicationName);
        }
        return pu;
    }

    private static String[] extractIpAddresses(ProcessingUnitInstance[] instances) {
        final List<String> ipAddresses = new ArrayList<String>(instances.length);
        for (final ProcessingUnitInstance instance : instances) {
            ipAddresses.add(instance.getMachine().getHostAddress());
        }
        return ipAddresses.toArray(new String[ipAddresses.size()]);
    }


    /**
     * This implementation resolves hostProperty into hostAddress
     */
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String value = null;
        if (placeholder.equals(outputProperty)) {
            if (ipAddresses == null) {
                throw new IllegalStateException("Bean not initialized yet");
            }
            value = StringUtils.arrayToCommaDelimitedString(ipAddresses);
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved " + placeholder +"="+value);
            }
        }
        return value;
    }

}