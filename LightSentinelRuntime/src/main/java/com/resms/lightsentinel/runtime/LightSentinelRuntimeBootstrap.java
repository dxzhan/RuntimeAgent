package com.resms.lightsentinel.runtime;

import com.resms.lightsentinel.common.AbstractLightSentinelBootstrap;
import com.resms.lightsentinel.common.LightSentinelException;
import com.resms.lightsentinel.common.LightSentinelBootstrap;
import com.resms.lightsentinel.common.LightSentinelThreadFactory;
import com.resms.lightsentinel.common.event.LivenessEvent;
import com.resms.lightsentinel.common.vm.LightSentinelVmRegistry;
import com.resms.lightsentinel.common.meta.LightSentinelMetaProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

/**
 * LightSentinel管理器
 *
 * @author sam
 */
@Component
public class LightSentinelRuntimeBootstrap extends AbstractLightSentinelBootstrap implements LightSentinelBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelRuntimeBootstrap.class);

    private LightSentinelVmRegistry registry;

    @Autowired
    public LightSentinelRuntimeBootstrap(ApplicationContext applicationContext, LightSentinelMetaProperties properties) {
        super(applicationContext,properties);
        registry = new LightSentinelVmRegistry(zkClient, getRVmPath(),properties);
    }

    @Override
    public void afterStart() {
        try {
            registry.start();

            if(getZkClient() !=null && getZkClient().checkExists().forPath(getRVmPath()) == null){
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getRVmPath());
                logger.info("register RVmPath is {}", getRVmPath());
            }

            // 注册上行事件ZK路径，用于vm发布事件
            if (getZkClient() != null && getZkClient().checkExists().forPath(getRVmUpEventPath()) == null) {
                getZkClient().create().creatingParentsIfNeeded().forPath(getRVmUpEventPath());
                logger.info("register VmUpEventPath is {}", getRVmUpEventPath());
            }

            // 注册下行事件ZK路径，用于vm接收事件
            if (getZkClient() != null && getZkClient().checkExists().forPath(getRVmDownEventPath()) == null) {
                getZkClient().create().creatingParentsIfNeeded().forPath(getRVmDownEventPath());
                logger.info("register VmDownEventPath is {}", getRVmDownEventPath());
            }
            // 给vm注册对应下行事件的数据变更监听器
            CuratorCache watcher = CuratorCache.build(
                    zkClient,
                    getRVmDownEventPath()
            );

            //添加当前vm节点下行事件节点的变化监听器
            watcher.listenable().addListener(CuratorCacheListener.builder().
                    forPathChildrenCache(getRVmDownEventPath(), zkClient, new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                            if (event != null) {
                                ChildData data = event.getData();
                                if (data != null) {
                                    switch (event.getType()) {
                                        case CHILD_ADDED:
                                            vmDataChangeEventPathList.add(data.getPath());
                                            break;
                                        case CHILD_REMOVED:
                                            vmDataChangeEventPathList.remove(data.getPath());
                                            break;
                                    }
                                    if ((CHILD_ADDED.equals(event.getType()) || CHILD_UPDATED.equals(event.getType())) && data.getData() != null) {
                                        dispatchEvent(data.getPath(),data.getData());
                                    }
                                }
                            }
                        }
                    }).build());
            watcher.start();
            registerEventListener();
        } catch (Exception e) {
            logger.error("registerVm exception",e);
            throw new LightSentinelException(e);
        }
    }
    private void test() {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new LightSentinelThreadFactory("RTARuntimeLiveThread-",true));
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //logger.debug("scheduleWithFixedDelay run at " + System.currentTimeMillis());
                publishLightSentinelEvent(LivenessEvent.success(properties.getNodeId()));
            }
        },HEART_DELAY,HEART_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        super.close();
        if (registry != null) {
            registry.close();
        }
    }

    @Override
    public void registerEventListener() throws Exception {
        registerEventListener(getLightSentinelDownEventPath());
    }

    /**
     * 向VM上行事件路径发布事件（写数据）
     * @param object
     */
    public void publishVmEvent(Object object) {
        publishVmEvent(getRVmUpEventPath(),object);
    }

    /**
     * 向event事件路径发布事件（写数据）,用于runtime向manager推送事件
     * @param object
     */
    public void publishLightSentinelEvent(Object object) {
        publishLightSentinelEvent(getLightSentinelUpEventPath(),object);
    }

    public LightSentinelVmRegistry getRegistry() {
        return registry;
    }
}