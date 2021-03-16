package com.resms.rta.common.vm;

import com.resms.rta.common.meta.RTAMetaProperties;
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
public class RTAVmRegistry implements Closeable
{
    private static final Logger logger = LoggerFactory.getLogger(RTAVmRegistry.class);
    private final ServiceDiscovery<VmDetail> vmDiscovery;
    private final ServiceInstance<VmDetail> thisInstance;
    private final CuratorFramework client;
    private final String basePath;
    private final RTAMetaProperties properties;

    public RTAVmRegistry(CuratorFramework client, String basePath, RTAMetaProperties properties)
    {
        ServiceInstance<VmDetail> thisInstance1;
        this.client = client;
        this.basePath = basePath;
        this.properties = properties;

        try {
            thisInstance1 = ServiceInstance.<VmDetail>builder()
                    .name(properties.getPid())
                    .payload(new VmDetail(properties.getApp(),
                            properties.getType(),
                            properties.getPid(),
                            properties.getAddress(),
                            properties.getPort(),
                            properties.getDesc()))
                    .build();

        } catch (Exception e) {
            thisInstance1 = null;
        }
        thisInstance = thisInstance1;
        vmDiscovery = ServiceDiscoveryBuilder.builder(VmDetail.class)
                .client(this.client)
                .basePath(this.basePath)
                .serializer(new JsonInstanceSerializer<VmDetail>(VmDetail.class))
                .thisInstance(thisInstance)
                .build();
    }

    public ServiceInstance<VmDetail> getThisInstance()
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
                logger.error("RTAVmRegistry start failure",e);
            }
        } else {
            logger.error("instance is null and RTAVmRegistry start failure");
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