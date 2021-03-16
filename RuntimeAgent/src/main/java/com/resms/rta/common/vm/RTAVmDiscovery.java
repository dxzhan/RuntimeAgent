package com.resms.rta.common.vm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.resms.rta.common.meta.RTAMetaProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 嵌入应用用于VM实例发现
 *
 * @author sam
 */
public class RTAVmDiscovery
{
    private static final Logger logger = LoggerFactory.getLogger(RTAVmDiscovery.class);
    private CuratorFramework client = null;
    private ServiceDiscovery<VmDetail> vmDiscovery = null;
    private Map<String, ServiceProvider<VmDetail>> providers = Maps.newConcurrentMap();
    private Object lock = new Object();
    private String basePath;
    private RTAMetaProperties properties;

    public RTAVmDiscovery(CuratorFramework client, String basePath, RTAMetaProperties properties)
    {
            this.client = client;
            this.properties = properties;
            this.basePath = basePath;

            vmDiscovery = ServiceDiscoveryBuilder.builder(VmDetail.class)
                    .client(client)
                    .basePath(basePath)
                    .serializer(new JsonInstanceSerializer<VmDetail>(VmDetail.class))
                    .build();
    }

    public VmDetail getVmDetail(String pid) throws Exception {
        ServiceInstance<VmDetail> instance = getInstanceByName(pid);
        return instance.getPayload();
    }

    private ServiceInstance<VmDetail> getInstanceByName(String pid) throws Exception {
        ServiceProvider<VmDetail> provider = providers.get(pid);
        if (provider == null) {
            synchronized (lock) {
                provider = providers.get(pid);
                if (provider == null) {
                    provider = vmDiscovery.serviceProviderBuilder()
                            .serviceName(pid)
                            .providerStrategy(new RandomStrategy<VmDetail>())
                            .build();
                    provider.start();
                    providers.put(pid, provider);
                }
            }
        }

        return provider.getInstance();
    }

    public Collection<String> getVmNameList() {
        try {
            return vmDiscovery.queryForNames();
        } catch (Exception e) {
            logger.error("getVmNameList failure",e);
            return null;
        }
    }

    public List<VmDetail> getVms() {
        return getInstances().stream().map(i->i.getPayload()).collect(Collectors.toList());
    }

    private List<ServiceInstance<VmDetail>> getInstances() {
        List<ServiceInstance<VmDetail>> vms = Lists.newArrayList();
        try {
            for (String serviceName : vmDiscovery.queryForNames()) {
                vms.addAll(vmDiscovery.queryForInstances(serviceName));
            }
        } catch (Exception e) {
            logger.error("queryForNames or queryForInstances failure",e);
        }
        return vms;
    }

    public void start() {
        try {
            vmDiscovery.start();
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    public synchronized void close(){
        providers.values().stream().forEach(p -> CloseableUtils.closeQuietly(p));
        CloseableUtils.closeQuietly(vmDiscovery);
    }
}