package com.resms.lightsentinel.common.vm;

import com.resms.lightsentinel.common.meta.LightSentinelMetaProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * 嵌入应用用于VM实例注册
 *
 * @author sam
 */
public class LightSentinelVmRegistry implements Closeable
{
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelVmRegistry.class);
    private final ServiceDiscovery<LightSentinelDetail> vmDiscovery;
    private final ServiceInstance<LightSentinelDetail> thisInstance;
    private final CuratorFramework client;
    private final String basePath;
    private final LightSentinelMetaProperties properties;

    public LightSentinelVmRegistry(CuratorFramework client, String basePath, LightSentinelMetaProperties properties)
    {
        ServiceInstance<LightSentinelDetail> thisInstance1;
        this.client = client;
        this.basePath = basePath;
        this.properties = properties;

        try {
            thisInstance1 = ServiceInstance.<LightSentinelDetail>builder()
                    .name(properties.getNodeId())
                    .payload(new LightSentinelDetail(properties.getApp(),
                            properties.getType(),
                            properties.getNodeId(),
                            properties.getAddress(),
                            properties.getPort(),
                            properties.getDesc()))
                    .build();

        } catch (Exception e) {
            thisInstance1 = null;
        }
        thisInstance = thisInstance1;
        vmDiscovery = ServiceDiscoveryBuilder.builder(LightSentinelDetail.class)
                .client(this.client)
                .basePath(this.basePath)
                .serializer(new JsonInstanceSerializer<LightSentinelDetail>(LightSentinelDetail.class))
                .thisInstance(thisInstance)
                .build();
    }

    public ServiceInstance<LightSentinelDetail> getThisInstance()
    {
        return thisInstance;
    }

//    public void registerVm(ServiceInstance<VmDetail> vmInstance) throws Exception {
//        vmDiscovery.registerService(vmInstance);
//    }
//
//    public void unregisterVm(ServiceInstance<VmDetail> vmInstance) throws Exception {
//        vmDiscovery.unregisterService(vmInstance);
//    }
//
//    public void updateVm(ServiceInstance<VmDetail> vmInstance) throws Exception {
//        vmDiscovery.updateService(vmInstance);
//    }

    public void start()
    {
        if (thisInstance != null) {
            try {
                vmDiscovery.start();
            } catch (Exception e) {
                logger.error("LightSentinelVmRegistry start failure",e);
            }
        } else {
            logger.error("instance is null and LightSentinelVmRegistry start failure");
        }
    }

    @Override
    public void close()
    {
        if (vmDiscovery != null) {
            CloseableUtils.closeQuietly(vmDiscovery);
        }
    }
}