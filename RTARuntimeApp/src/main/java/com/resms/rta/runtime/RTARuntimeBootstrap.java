package com.resms.rta.runtime;

import com.resms.rta.common.AbstractRTABootstrap;
import com.resms.rta.common.RTAException;
import com.resms.rta.common.RTABootstrap;
import com.resms.rta.common.RTAThreadFactory;
import com.resms.rta.common.event.LivenessEvent;
import com.resms.rta.common.vm.RTAVmRegistry;
import com.resms.rta.common.meta.RTAMetaProperties;
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
 * RTA管理器
 *
 * @author sam
 */
@Component
public class RTARuntimeBootstrap extends AbstractRTABootstrap implements RTABootstrap {
    private static final Logger logger = LoggerFactory.getLogger(RTARuntimeBootstrap.class);

    private RTAVmRegistry registry;

    @Autowired
    public RTARuntimeBootstrap(ApplicationContext applicationContext, RTAMetaProperties properties) {
        super(applicationContext,properties);
        registry = new RTAVmRegistry(zkClient, getRVmPath(),properties);
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
            throw new RTAException(e);
        }
    }
    private void test() {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new RTAThreadFactory("RTARuntimeLiveThread-",true));
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //logger.debug("scheduleWithFixedDelay run at " + System.currentTimeMillis());
                publishRTAEvent(LivenessEvent.success(properties.getPid()));
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
        registerEventListener(getRTADownEventPath());
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
    public void publishRTAEvent(Object object) {
        publishRTAEvent(getRTAUpEventPath(),object);
    }

    public RTAVmRegistry getRegistry() {
        return registry;
    }
}