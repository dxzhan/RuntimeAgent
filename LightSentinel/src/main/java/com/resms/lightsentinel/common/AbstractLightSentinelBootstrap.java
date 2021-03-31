package com.resms.lightsentinel.common;

import com.resms.lightsentinel.common.event.AbstractLightSentinelEvent;
import com.resms.lightsentinel.common.handler.LightSentinelEventHandler;
import com.resms.lightsentinel.common.handler.LightSentinelEventRegistry;
import com.resms.lightsentinel.common.meta.LightSentinelMetaProperties;
import com.resms.lightsentinel.common.util.ProtobufUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_ADDED;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_UPDATED;

/**
 * 抽象LightSentinelBoostrap类，用于启动管理LightSentinel
 *
 * @author sam
 */
public abstract class AbstractLightSentinelBootstrap implements LightSentinelBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLightSentinelBootstrap.class);

    protected ApplicationContext applicationContext;
    protected LightSentinelEventRegistry registry;
    protected boolean running = false;

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * LightSentinel元数据定义
     */
    protected LightSentinelMetaProperties properties;

    /**
     * 心跳延迟时间，单位秒
     */
    protected int HEART_DELAY = 10;

    /**
     * 心跳调度服务
     */
//    protected ScheduledExecutorService scheduledExecutorService;

    /**
     * ZK客户端
     */
    protected CuratorFramework zkClient;

    /**
     * 注册了vm下行事件数据状态的路径列表
     */
    protected List<String> vmDataChangeEventPathList = new ArrayList<>();

    /**
     * 注册了LightSentinel下行事件数据状态的路径列表
     */
    protected List<String> lightSentinelDataChangeEventPathList = new ArrayList<>();

    public AbstractLightSentinelBootstrap(ApplicationContext applicationContext, LightSentinelMetaProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;

        //设置心跳间隔
        HEART_DELAY = properties.getHeartDelay();

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(properties.getZkAddress())
                .sessionTimeoutMs(properties.getZkSessionTimeout())
                .connectionTimeoutMs(properties.getZkConnectionTimeout())
                .retryPolicy(new RetryNTimes(properties.getRetry(), properties.getRetryDuration()))
                .build();
//        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new LightSentinelThreadFactory("LightSentinelLiveThread-",true));
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public CuratorFramework getZkClient() {
        return zkClient;
    }

    public String getVMType() {
        return properties.getType();
    }

    @Override
    public void start() throws LightSentinelException {
        if (running) {
            logger.warn("bootstrap already started!");
            return;
        }
        try {
            zkClient.start();
            logger.info("zkClient started!");
            //初始化LightSentinel存储库
            initStorege();
            afterStart();
            running = true;
        } catch (IllegalStateException e) {
            CloseableUtils.closeQuietly(this);
        } catch (LightSentinelException e) {
            CloseableUtils.closeQuietly(this);
        } catch (Exception e) {
            CloseableUtils.closeQuietly(this);
        }
//        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                //logger.debug("scheduleWithFixedDelay run at " + System.currentTimeMillis());
//                applicationContext.publishEvent(LivenessEvent.success());
//            }
//        },HEART_DELAY,HEART_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public abstract void afterStart();

    /**
     * 初始化LightSentinel存储库（ZK）
     */
    public void initStorege() {
        try {
            if (getZkClient() != null && getZkClient().checkExists().forPath(LightSentinelConstant.LIGHT_SENTINEL_ROOT_PATH_NAME) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(LightSentinelConstant.LIGHT_SENTINEL_ROOT_PATH_NAME);
                logger.info("register LightSentinelRootPath is {}", LightSentinelConstant.LIGHT_SENTINEL_ROOT_PATH_NAME);
            }
            if (getZkClient() != null && getZkClient().checkExists().forPath(getAppPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getAppPath());
                logger.info("register AppPath is {}", getAppPath());
            }

            if (getZkClient() != null && getZkClient().checkExists().forPath(getLightSentinelEventPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getLightSentinelEventPath());
                logger.info("register LightSentinelEventPath is {}", getLightSentinelEventPath());
            }

            if (getZkClient() != null && getZkClient().checkExists().forPath(getLightSentinelUpEventPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getLightSentinelUpEventPath());
                logger.info("register LightSentinelUpEventPath is {}", getLightSentinelUpEventPath());
            }

            if (getZkClient() != null && getZkClient().checkExists().forPath(getLightSentinelDownEventPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getLightSentinelDownEventPath());
                logger.info("register LightSentinelDownEventPath is {}", getLightSentinelDownEventPath());
            }
        } catch (Exception e) {
            logger.error("initStorege failure",e);
        }
    }


    /**
     * 注册LightSentinel公共事件路径监听器，这里的LightSentinelEventPath具有相对意义，需要相对赋值，所以需要子类来指定
     * 推荐做法：
     *       受控端（Client）LightSentinelDownEventPath是指受控端的LightSentinel下行事件路径，也即控制端的LightSentinel上行事件路径，受控端在此路径注册数据变更监听器，监听此路径并接收控制端发布的事件。
     * 相对：
     *       控制端（Server）LightSentinelDownEventPath是指LightSentinel下行事件路径，也即受控端的上行事件路径，控制端在此路径注册数据变更监听器，监听此路径并接收受控端发布的事件。
     *
     * @param LightSentinelEventPath
     */
    public void registerEventListener(String LightSentinelEventPath) {
        try {
            // 注册下行事件ZK路径，用于vm接收事件
            if (getZkClient() != null && getZkClient().checkExists().forPath(LightSentinelEventPath) == null) {
                getZkClient().create().creatingParentsIfNeeded().forPath(LightSentinelEventPath);
                logger.info("register listener at LightSentinelEventPath is {}", LightSentinelEventPath);

            }
            CuratorCache watcher = CuratorCache.build(
                    zkClient,
                    LightSentinelEventPath
            );
            watcher.listenable().addListener(CuratorCacheListener.builder()
                    .forPathChildrenCache(LightSentinelEventPath, zkClient, new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    if (event != null) {
                        ChildData data = event.getData();
                        if (data != null) {
                            switch (event.getType()) {
                                case CHILD_ADDED:
                                    lightSentinelDataChangeEventPathList.add(data.getPath());
                                    break;
                                case CHILD_REMOVED:
                                    lightSentinelDataChangeEventPathList.remove(data.getPath());
                                    break;
                            }

                            if ((CHILD_ADDED.equals(event.getType()) || CHILD_UPDATED.equals(event.getType())) && data.getData() != null) {
                                dispatchEvent(data.getPath(), data.getData());
                            }
                        }
                    }
                }
            }).build());
            watcher.start();
        } catch (Exception e) {
            logger.error("registerEventListener error for path is " + LightSentinelEventPath,e);
        }
    }

    public void dispatchEvent(String path,byte[] data) {
        if (registry == null) {
            registry = applicationContext.getBean(LightSentinelEventRegistry.class);
        }
        //获得本地注册的事件插件
        String eventClassName = path.substring(path.lastIndexOf("/") + 1);
        LightSentinelEventHandler handler = registry.getHandlerByEvent(eventClassName);
        // 如果本地注册了对应事件的插件，则订阅该插件的数据变更事件，如果本地没有注册对应的事件插件，则忽略
        if (handler != null) {
            try {
                handler.onEvent((AbstractLightSentinelEvent) ProtobufUtil.deserializeFromByte(data, Class.forName(eventClassName)));
            } catch (ClassNotFoundException e) {
                logger.error("event class " + eventClassName + " notFound",e);
            }
        }
    }
    /**
     * 注册LightSentinelEvent监听器，由子类根据子类定位调用registerEventListener(String LightSentinelEventPath)实现
     */
    public abstract void registerEventListener() throws Exception;

    @Override
    public void publishVmEvent(String basePath,Object object) {
        if (getZkClient() != null) {
            String path = basePath + "/" + object.getClass().getName();
            try {
                if (getZkClient().checkExists().forPath(path) == null) {
                    getZkClient().create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(path, ProtobufUtil.serializeToByte(object));
                    logger.info("register VmEvent path is {}", path);
                } else {
                    getZkClient().setData().forPath(path, ProtobufUtil.serializeToByte(object));
                    logger.info("publish VmEvent and writeData to {}", path);
                }
            } catch (Exception e) {
                logger.error("publishVmEvent error and path is " + path,e);
            }
        } else {
            logger.warn("zkClient is null and publish event by {} failure",object.getClass().getName());
        }
    }

    /**
     * 向LightSentinel上行事件路径发布事件（写数据）,这里的LightSentinelUpEventPath具有相对意义，需要相对赋值，所以需要子类来指定
     * 推荐做法：
     *       受控端（Client）LightSentinelUpEventPath是指受控端的LightSentinel上行事件路径，也即控制端的LightSentinel下行事件路径，受控端经此路径发布事件给控制端。
     * 相对：
     *       控制端（Server）LightSentinelUpEventPath是指控制端的LightSentinel上行事件路径，也即受控端的下行事件路径，控制端经此路径发布事件给受控端。
     *
     * @param object 事件对象，需继承AbstractLightSentinelEvent
     */
    @Override
    public void publishLightSentinelEvent(String LightSentinelUpEventPath, Object object) {
        if (getZkClient() != null) {
            String path = LightSentinelUpEventPath + "/" + object.getClass().getName();
            try {
                if (getZkClient().checkExists().forPath(path) == null) {
                    getZkClient().create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(path, ProtobufUtil.serializeToByte(object));
                    logger.info("register LightSentinelEvent and writeData to path is {}", path);
                } else {
                    getZkClient().setData().forPath(path, ProtobufUtil.serializeToByte(object));
                    logger.info("publish LightSentinelEvent and writeData to {}", path);
                }
            } catch (Exception e) {
                logger.error("publishLightSentinelEvent error for path is " + LightSentinelUpEventPath,e);
            }
        } else {
            logger.warn("zkClient is null and publish event by {} failure",object.getClass().getName());
        }
    }

    @Override
    public void close()
    {
        if(zkClient != null) {
            zkClient.close();
        }
    }

    /**
     * 获得app路径
     * @return
     */
    public String getAppPath() {
        return LightSentinelConstant.LIGHT_SENTINEL_ROOT_PATH_NAME + "/" + properties.getApp();
    }

    /**
     * 获得rvm注册路径
     * @return
     */
    public String getRVmPath() {
        return getAppPath() + LightSentinelConstant.CVM_PATH_NAME;
    }

    /**
     * 获得rvm进程路径
     * @return
     */
    public String getRVmIdPath() {
        return getRVmPath() + "/" + properties.getNodeId();
    }

    /**
     * 获得rvm下行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getRVmDownEventPath() {
        return getRVmIdPath() + LightSentinelConstant.VM_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得rvm上行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getRVmUpEventPath() {
        return getRVmIdPath() + LightSentinelConstant.VM_UPEVENT_PATH_NAME;
    }

    /**
     * 获得mvm注册路径
     * @return
     */
    public String getMVmPath() {
        return getAppPath() + LightSentinelConstant.MVM_PATH_NAME;
    }

    /**
     * 获得mvm进程路径
     * @return
     */
    public String getMVmIdPath() {
        return getMVmPath() + "/" + properties.getNodeId();
    }

    /**
     * 获得mvm下行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getMVmDownEventPath() {
        return getMVmIdPath() + LightSentinelConstant.VM_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得mvm上行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getMVmUpEventPath() {
        return getMVmIdPath() + LightSentinelConstant.VM_UPEVENT_PATH_NAME;
    }

    /**
     * 获得LightSentinel公共事件路径
     * @return
     */
    public String getLightSentinelEventPath() {
        return getAppPath() + LightSentinelConstant.LIGHT_SENTINEL_EVENT_PATH_NAME;
    }

    /**
     * 获得RLightSentinel公共上行事件路径
     * @return
     */
    public String getLightSentinelUpEventPath() {
        return getLightSentinelEventPath() + LightSentinelConstant.LIGHT_SENTINEL_UPEVENT_PATH_NAME;
    }

    /**
     * 获得LightSentinel公共下行事件路径
     * @return
     */
    public String getLightSentinelDownEventPath() {
        return getLightSentinelEventPath() + LightSentinelConstant.LIGHT_SENTINEL_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得LightSentinel服务路径
     * @return
     */
    public String getLightSentinelServicePath() {
        return getAppPath() + LightSentinelConstant.LIGHT_SENTINEL_SERVICE_PATH_NAME;
    }
}