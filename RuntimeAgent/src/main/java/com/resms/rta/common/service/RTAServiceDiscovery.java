package com.resms.rta.common.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 嵌入应用用于服务实例发现
 *
 * @author sam
 */
public class RTAServiceDiscovery
{
    private CuratorFramework client = null;
    private ServiceDiscovery<ServiceInstanceDetail> serviceDiscovery = null;
    private Map<String, ServiceProvider<ServiceInstanceDetail>> providers = Maps.newConcurrentMap();
    private Object lock = new Object();
    private String basePath;

    public RTAServiceDiscovery(CuratorFramework client, String basePath) throws Exception
    {
            this.client = client;
            this.basePath = basePath;

            serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInstanceDetail.class)
                    .client(client)
                    .basePath(basePath)
                    .serializer(new JsonInstanceSerializer<ServiceInstanceDetail>(ServiceInstanceDetail.class))
                    .build();
    }

    public ServiceInstanceDetail getServiceInstanceDetail(String pid) throws Exception {
        ServiceInstance<ServiceInstanceDetail> instance = getServiceInstanceByName(pid);
        return instance.getPayload();
    }

    private ServiceInstance<ServiceInstanceDetail> getServiceInstanceByName(String pid) throws Exception {
        ServiceProvider<ServiceInstanceDetail> provider = providers.get(pid);
        if (provider == null) {
            synchronized (lock) {
                provider = providers.get(pid);
                if (provider == null) {
                    provider = serviceDiscovery.serviceProviderBuilder()
                            .serviceName(pid)
                            .providerStrategy(new RandomStrategy<ServiceInstanceDetail>())
                            .build();
                    provider.start();
                    providers.put(pid, provider);
                }
            }
        }

        return provider.getInstance();
    }

    public Collection<String> getServiceNameList() throws Exception {
        return serviceDiscovery.queryForNames();
    }

    public List<ServiceInstanceDetail> getServiceInstanceDetails() throws Exception {
        return getServiceInstances().stream().map(i->i.getPayload()).collect(Collectors.toList());
    }

    private List<ServiceInstance<ServiceInstanceDetail>> getServiceInstances() throws Exception {
        List<ServiceInstance<ServiceInstanceDetail>> vms = Lists.newArrayList();
        for ( String serviceName : serviceDiscovery.queryForNames() )
        {
            vms.addAll(serviceDiscovery.queryForInstances(serviceName));
        }
        return vms;
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    public synchronized void close(){
        providers.values().stream().forEach(p -> CloseableUtils.closeQuietly(p));
        CloseableUtils.closeQuietly(serviceDiscovery);
    }
}