package com.resms.lightsentinel.common.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * 嵌入应用用于服务实例注册
 *
 * @author sam
 */
public class LightSentinelServiceRegistry implements Closeable
{
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelServiceRegistry.class);
    private final ServiceDiscovery<ServiceInstanceDetail> serviceDiscovery;
    private final CuratorFramework client;
    private final String basePath;
    private boolean running = false;

    public LightSentinelServiceRegistry(CuratorFramework client, String basePath) throws Exception
    {
        this.client = client;
        this.basePath = basePath;

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInstanceDetail.class)
            .client(this.client)
            .basePath(this.basePath)
            .serializer(new JsonInstanceSerializer<ServiceInstanceDetail>(ServiceInstanceDetail.class))
            .build();
    }

    public void registerService(ServiceInstance<ServiceInstanceDetail> serviceInstance) throws Exception {
        if (isRunning()) {
            serviceDiscovery.registerService(serviceInstance);
        } else {
            logger.warn("LightSentinelServiceRegistry is not running");
        }
    }

    public void unregisterService(ServiceInstance<ServiceInstanceDetail> serviceInstance) throws Exception {
        if (isRunning()) {
        serviceDiscovery.unregisterService(serviceInstance);
        } else {
            logger.warn("LightSentinelServiceRegistry is not running");
        }
    }

    public void updateService(ServiceInstance<ServiceInstanceDetail> serviceInstance) throws Exception {
        if (isRunning()) {
        serviceDiscovery.updateService(serviceInstance);
        } else {
            logger.warn("LightSentinelServiceRegistry is not running");
        }
    }

    public Collection<ServiceInstance<ServiceInstanceDetail>> queryForServiceInstances(String name) throws Exception {
        if (isRunning()) {
        return serviceDiscovery.queryForInstances(name);
        } else {
            logger.warn("LightSentinelServiceRegistry is not running");
        }
        return null;
    }

    public ServiceInstance<ServiceInstanceDetail> queryForVm(String name, String id) throws Exception {
        if (isRunning()) {
            return serviceDiscovery.queryForInstance(name, id);
        } else {
            logger.warn("LightSentinelServiceRegistry is not running");
        }
        return null;
    }

    public void start() throws Exception
    {
        serviceDiscovery.start();
        running = true;
    }

    @Override
    public void close() throws IOException
    {
        running = false;
        CloseableUtils.closeQuietly(serviceDiscovery);
    }

    public boolean isRunning() {
        return running;
    }
}