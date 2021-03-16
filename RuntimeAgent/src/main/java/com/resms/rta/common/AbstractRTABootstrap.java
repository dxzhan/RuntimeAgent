package com.resms.rta.common;

import com.resms.rta.common.event.AbstractRTAEvent;
import com.resms.rta.common.meta.RTAMetaProperties;
import com.resms.rta.common.plugin.RTAEventPlugin;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
import com.resms.rta.common.util.JsonMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

/**
 * 抽象RTABoostrap类，用于启动管理RTA
 *
 * @author sam
 */
public abstract class AbstractRTABootstrap implements RTABootstrap {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRTABootstrap.class);

    protected ApplicationContext applicationContext;
    protected RTAEventPluginFactory factory;
    protected boolean running = false;

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * RTA元数据定义
     */
    protected RTAMetaProperties properties;

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
     * 注册了RTA下行事件数据状态的路径列表
     */
    protected List<String> rtaDataChangeEventPathList = new ArrayList<>();

    public AbstractRTABootstrap(ApplicationContext applicationContext, RTAMetaProperties properties) {
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
//        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new RTAThreadFactory("RTALiveThread-",true));
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
    public void start() throws RTAException {
        //logger.info("properties:" + JsonMapper.toJson(properties));
        if (running) {
            logger.warn("bootstrap already started!");
            return;
        }
        try {
            zkClient.start();
            logger.info("zkClient started!");
            //初始化RTA存储库
            initStorege();
            afterStart();
            running = true;
        } catch (IllegalStateException e) {
            CloseableUtils.closeQuietly(this);
        } catch (RTAException e) {
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
     * 初始化RTA存储库（ZK）
     */
    public void initStorege() {
        try {
            if (getZkClient() != null && getZkClient().checkExists().forPath(RTAConstant.RTA_ROOT_PATH_NAME) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(RTAConstant.RTA_ROOT_PATH_NAME);
                logger.info("register RTARootPath is {}", RTAConstant.RTA_ROOT_PATH_NAME);
            }
            if (getZkClient() != null && getZkClient().checkExists().forPath(getAppPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getAppPath());
                logger.info("register AppPath is {}", getAppPath());
            }

            if (getZkClient() != null && getZkClient().checkExists().forPath(getRTAEventPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getRTAEventPath());
                logger.info("register RTAEventPath is {}", getRTAEventPath());
            }

            if (getZkClient() != null && getZkClient().checkExists().forPath(getRTAUpEventPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getRTAUpEventPath());
                logger.info("register RTAUpEventPath is {}", getRTAUpEventPath());
            }

            if (getZkClient() != null && getZkClient().checkExists().forPath(getRTADownEventPath()) == null) {
                getZkClient().create()
                        .creatingParentsIfNeeded()
                        .forPath(getRTADownEventPath());
                logger.info("register RTADownEventPath is {}", getRTADownEventPath());
            }
        } catch (Exception e) {
            logger.error("initStorege failure",e);
        }
    }


    /**
     * 注册RTA公共事件路径监听器，这里的RTAEventPath具有相对意义，需要相对赋值，所以需要子类来指定
     * 推荐做法：
     *       受控端（Client）RTADownEventPath是指受控端的RTA下行事件路径，也即控制端的RTA上行事件路径，受控端在此路径注册数据变更监听器，监听此路径并接收控制端发布的事件。
     * 相对：
     *       控制端（Server）RTADownEventPath是指RTA下行事件路径，也即受控端的上行事件路径，控制端在此路径注册数据变更监听器，监听此路径并接收受控端发布的事件。
     *
     * @param RTAEventPath
     */
    public void registerEventListener(String RTAEventPath) {
        try {
            // 注册下行事件ZK路径，用于vm接收事件
            if (getZkClient() != null && getZkClient().checkExists().forPath(RTAEventPath) == null) {
                getZkClient().create().creatingParentsIfNeeded().forPath(RTAEventPath);
                logger.info("register listener at RTAEventPath is {}", RTAEventPath);

            }
            CuratorCache watcher = CuratorCache.build(
                    zkClient,
                    RTAEventPath
            );
            watcher.listenable().addListener(CuratorCacheListener.builder()
                    .forPathChildrenCache(RTAEventPath, zkClient, new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    if (event != null) {
                        ChildData data = event.getData();
                        if (data != null) {
                            switch (event.getType()) {
                                case CHILD_ADDED:
                                    rtaDataChangeEventPathList.add(data.getPath());
                                    break;
                                case CHILD_REMOVED:
                                    rtaDataChangeEventPathList.remove(data.getPath());
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
            logger.error("registerEventListener error for path is " + RTAEventPath,e);
        }
    }

    public void dispatchEvent(String path,byte[] data) {
        if (factory == null) {
            factory = applicationContext.getBean(RTAEventPluginFactory.class);
        }
        //获得本地注册的事件插件
        String eventClassName = path.substring(path.lastIndexOf("/") + 1);
        RTAEventPlugin plugin = factory.getPluginByEvent(eventClassName);
        // 如果本地注册了对应事件的插件，则订阅该插件的数据变更事件，如果本地没有注册对应的事件插件，则忽略
        if (plugin != null) {
            try {
                plugin.onEvent((AbstractRTAEvent) JsonMapper.parse(new String(data, StandardCharsets.UTF_8), Class.forName(eventClassName)));
            } catch (ClassNotFoundException e) {
                logger.error("event class " + eventClassName + " notFound",e);
            }
        }
    }
    /**
     * 注册RTAEvent监听器，由子类根据子类定位调用registerEventListener(String RTAEventPath)实现
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
                            .forPath(path, JsonMapper.toJsonBytes(object));
                    logger.info("register VmEvent path is {}", path);
                } else {
                    getZkClient().setData().forPath(path, JsonMapper.toJsonBytes(object));
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
     * 向RTA上行事件路径发布事件（写数据）,这里的RTAUpEventPath具有相对意义，需要相对赋值，所以需要子类来指定
     * 推荐做法：
     *       受控端（Client）RTAUpEventPath是指受控端的RTA上行事件路径，也即控制端的RTA下行事件路径，受控端经此路径发布事件给控制端。
     * 相对：
     *       控制端（Server）RTAUpEventPath是指控制端的RTA上行事件路径，也即受控端的下行事件路径，控制端经此路径发布事件给受控端。
     *
     * @param object 事件对象，需继承AbstractRTAEvent
     */
    @Override
    public void publishRTAEvent(String RTAEventPath, Object object) {
        if (getZkClient() != null) {
            String path = RTAEventPath + "/" + object.getClass().getName();
            try {
                if (getZkClient().checkExists().forPath(path) == null) {
                    getZkClient().create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(path, JsonMapper.toJsonBytes(object));
                    logger.info("register RTAEvent and writeData to path is {}", path);
                } else {
                    getZkClient().setData().forPath(path, JsonMapper.toJsonBytes(object));
                    logger.info("publish RTAEvent and writeData to {}", path);
                }
            } catch (Exception e) {
                logger.error("publishRTAEvent error for path is " + RTAEventPath,e);
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
        return RTAConstant.RTA_ROOT_PATH_NAME + "/" + properties.getApp();
    }

    /**
     * 获得rvm注册路径
     * @return
     */
    public String getRVmPath() {
        return getAppPath() + RTAConstant.CVM_PATH_NAME;
    }

    /**
     * 获得rvm进程路径
     * @return
     */
    public String getRVmIdPath() {
        return getRVmPath() + "/" + properties.getPid();
    }

    /**
     * 获得rvm下行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getRVmDownEventPath() {
        return getRVmIdPath() + RTAConstant.VM_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得rvm上行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getRVmUpEventPath() {
        return getRVmIdPath() + RTAConstant.VM_UPEVENT_PATH_NAME;
    }

    /**
     * 获得mvm注册路径
     * @return
     */
    public String getMVmPath() {
        return getAppPath() + RTAConstant.MVM_PATH_NAME;
    }

    /**
     * 获得mvm进程路径
     * @return
     */
    public String getMVmIdPath() {
        return getMVmPath() + "/" + properties.getPid();
    }

    /**
     * 获得mvm下行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getMVmDownEventPath() {
        return getMVmIdPath() + RTAConstant.VM_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得mvm上行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getMVmUpEventPath() {
        return getMVmIdPath() + RTAConstant.VM_UPEVENT_PATH_NAME;
    }

    /**
     * 获得RTA公共事件路径
     * @return
     */
    public String getRTAEventPath() {
        return getAppPath() + RTAConstant.RTA_EVENT_PATH_NAME;
    }

    /**
     * 获得RTA公共上行事件路径
     * @return
     */
    public String getRTAUpEventPath() {
        return getRTAEventPath() + RTAConstant.RTA_UPEVENT_PATH_NAME;
    }

    /**
     * 获得RTA公共下行事件路径
     * @return
     */
    public String getRTADownEventPath() {
        return getRTAEventPath() + RTAConstant.RTA_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得RTA服务路径
     * @return
     */
    public String getRTAServicePath() {
        return getAppPath() + RTAConstant.RTA_SERVICE_PATH_NAME;
    }
}