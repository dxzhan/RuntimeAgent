package com.resms.lightsentinel.common.vm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.resms.lightsentinel.common.meta.LightSentinelMetaProperties;
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
public class LightSentinelVmDiscovery
{
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelVmDiscovery.class);
    private CuratorFramework client = null;
    private ServiceDiscovery<LightSentinelDetail> vmDiscovery = null;
    private Map<String, ServiceProvider<LightSentinelDetail>> providers = Maps.newConcurrentMap();
    private Object lock = new Object();
    private String basePath;
    private LightSentinelMetaProperties properties;

    public LightSentinelVmDiscovery(CuratorFramework client, String basePath, LightSentinelMetaProperties properties)
    {
            this.client = client;
            this.properties = properties;
            this.basePath = basePath;

            vmDiscovery = ServiceDiscoveryBuilder.builder(LightSentinelDetail.class)
                    .client(client)
                    .basePath(basePath)
                    .serializer(new JsonInstanceSerializer<LightSentinelDetail>(LightSentinelDetail.class))
                    .build();
    }

    public LightSentinelDetail getVmDetail(String nodeId) throws Exception {
        ServiceInstance<LightSentinelDetail> instance = getInstanceByName(nodeId);
        return instance.getPayload();
    }

    private ServiceInstance<LightSentinelDetail> getInstanceByName(String nodeId) throws Exception {
        ServiceProvider<LightSentinelDetail> provider = providers.get(nodeId);
        if (provider == null) {
            synchronized (lock) {
                provider = providers.get(nodeId);
                if (provider == null) {
                    provider = vmDiscovery.serviceProviderBuilder()
                            .serviceName(nodeId)
                            .providerStrategy(new RandomStrategy<LightSentinelDetail>())
                            .build();
                    provider.start();
                    providers.put(nodeId, provider);
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

    public List<LightSentinelDetail> getVms() {
        return getInstances().stream().map(i->i.getPayload()).collect(Collectors.toList());
    }

    private List<ServiceInstance<LightSentinelDetail>> getInstances() {
        List<ServiceInstance<LightSentinelDetail>> vms = Lists.newArrayList();
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