package com.resms.rta.common;

import com.resms.rta.common.event.LivenessEvent;
import com.resms.rta.common.meta.RTAMetaProperties;
import com.resms.rta.common.plugin.RTAEventPlugin;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
import com.resms.rta.common.util.JsonMapper;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRTAManager implements RTAManager{
    private static final Logger logger = LoggerFactory.getLogger(AbstractRTAManager.class);

    protected ApplicationContext applicationContext;

    /**
     * RTA元数据定义
     */
    protected RTAMetaProperties properties;
    /**
     * ZK客户端
     */
    protected ZkClient zkClient;

    /**
     * 心跳延迟时间，单位秒
     */
    protected int HEART_DELAY = 10;

    /**
     * 心跳调度服务
     */
    protected ScheduledExecutorService scheduledExecutorService;

    protected final EventDataChangeListener EVENT_DATA_CHANGE_LISTENER;
    /**
     * 注册了vm下行事件数据状态的路径列表
     */
    protected List<String> vmDataChangeEventPathList = new ArrayList<>();

    /**
     * 注册了RTA下行事件数据状态的路径列表
     */
    protected List<String> rtaDataChangeEventPathList = new ArrayList<>();

    static class EventDataChangeListener implements IZkDataListener {
        private ApplicationContext applicationContext;
        public EventDataChangeListener(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }
        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {
            logger.info("DownEventDataChangeListener: DataChange trigger and dataPath is {}",dataPath);
            applicationContext.publishEvent(data);
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            logger.info("DownEventDataChangeListener: DataDeleted trigger and dataPaht is {}",dataPath);
        }
    }

    public AbstractRTAManager(ApplicationContext applicationContext,RTAMetaProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        //设置心跳间隔
        HEART_DELAY = properties.getHeartDelay();

        EVENT_DATA_CHANGE_LISTENER = new EventDataChangeListener(applicationContext);
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new RTAThreadFactory("RTALiveThread-",true));
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ZkClient getZkClient() {
        return zkClient;
    }


    @Override
    public void start() throws RTAException {
        //logger.info("properties:" + JsonMapper.toJson(properties));
        try {
            zkClient = new ZkClient(properties.getZkAddress(), Integer.MAX_VALUE, properties.getZkConnectionTimeout());
            //初始化RTA存储库
            initRtaStorege();
        } catch (ZkInterruptedException | ZkTimeoutException | IllegalStateException e) {
            zkClient = null;
            throw new RTAException(e);
        } catch (RTAException e) {
            zkClient = null;
            throw e;
        } catch (Exception e) {
            zkClient = null;
            throw new RTAException(e);
        }
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //logger.debug("scheduleWithFixedDelay run at " + System.currentTimeMillis());
                applicationContext.publishEvent(LivenessEvent.success());
            }
        },HEART_DELAY,HEART_DELAY, TimeUnit.SECONDS);
    }

    /**
     * 初始化RTA存储库（ZK）
     */
    public void initRtaStorege() {
        if (getZkClient() !=null && !getZkClient().exists(RTAConstant.RTA_ROOT_PATH_NAME)) {
            getZkClient().createPersistent(RTAConstant.RTA_ROOT_PATH_NAME, true);
            logger.info("register RTARootPath is {}",RTAConstant.RTA_ROOT_PATH_NAME);
        }
        if (getZkClient() !=null && !getZkClient().exists(getAppPath())) {
            //创建APP级
            getZkClient().createPersistent(getAppPath(), true);
            logger.info("register AppPath is {}",getAppPath());
        }
        if(getZkClient() !=null && !getZkClient().exists(getVmPath())){
            getZkClient().createPersistent(getVmPath(), true);
            logger.info("register VmPath is {}",getVmPath());
        }

        if(getZkClient() !=null && !getZkClient().exists(getRTAUpEventPath())){
            getZkClient().createPersistent(getRTAUpEventPath(), true);
            logger.info("register RTAUpEventPath is {}",getRTAUpEventPath());
        }

        if(getZkClient() !=null && !getZkClient().exists(getRTADownEventPath())){
            getZkClient().createPersistent(getRTADownEventPath(), true);
            logger.info("register RTADownEventPath is {}",getRTADownEventPath());
        }

        unregisterVm();
        registerVm();
    }

    /**
     * 注册VM，创建ZK路径
     */
    @Override
    public void registerVm() {
        try {
            if(getZkClient() !=null && !getZkClient().exists(getVmIdPath())){
                getZkClient().createPersistent(getVmIdPath(), true);
                getZkClient().writeData(getVmIdPath(), JsonMapper.toJson(properties));
                logger.info("register VmIdPath is {}",getVmIdPath());
            }
            // 注册上行事件ZK路径，用于vm发布事件
            if (getZkClient() != null && !getZkClient().exists(getVmUpEventPath())) {
                getZkClient().createPersistent(getVmUpEventPath(), true);
                logger.info("register VmUpEventPath is {}", getVmUpEventPath());
            }
            // 注册下行事件ZK路径，用于vm接收事件
            if (getZkClient() != null && !getZkClient().exists(getVmDownEventPath())) {
                getZkClient().createPersistent(getVmDownEventPath(), true);
                logger.info("register VmDownEventPath is {}", getVmDownEventPath());
                // 注册对应下行事件的数据变更监听器
                getZkClient().subscribeChildChanges(getVmDownEventPath(), new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        logger.info("ChildChange size is {} and current listener size is {}",currentChilds.size(),getZkClient().numberOfListeners());
                        //currentChilds内即是自定义事件的全限定类名称的列表
                        if (vmDataChangeEventPathList.size() > 0) {
                            List<String> removelist = new ArrayList<>();
                            // 删除的事件需要解除数据变更监听器
                            vmDataChangeEventPathList
                                    .stream()
                                    .filter(p -> !currentChilds.contains(p))
                                    .forEach(p -> {
                                        removelist.add(p);
                                        getZkClient().unsubscribeDataChanges(parentPath + "/" + p, EVENT_DATA_CHANGE_LISTENER);
                                        logger.info("unsubscribeDataChange path is {} and current listener size is {}", parentPath + "/" + p,getZkClient().numberOfListeners());
                                    });
                            vmDataChangeEventPathList.removeAll(removelist);
                        }
                        // 新增且有对应注册的事件插件的事件需要添加数据变更监听器
                        if (currentChilds.size() > 0) {
                            List<String> addList = new ArrayList<>();
                            currentChilds
                                    .stream()
                                    .filter(p -> !vmDataChangeEventPathList.contains(p))
                                    .forEach(p -> {
                                        RTAEventPluginFactory factory = applicationContext.getBean(RTAEventPluginFactory.class);
                                        //获得本地注册的事件插件
                                        RTAEventPlugin plugin = factory.getPluginByEvent(p);
                                        // 如果本地注册了对应事件的插件，则订阅该插件的数据变更事件，如果本地没有注册对应的事件插件，则忽略
                                        if (plugin != null) {
                                            addList.add(p);
                                            getZkClient().subscribeDataChanges(parentPath + "/" + p, EVENT_DATA_CHANGE_LISTENER);
                                            logger.info("subscribeDataChange path is {} and current listener size is {}", parentPath + "/" + p,getZkClient().numberOfListeners());
                                            //如果是第一次创建DownEvent，进而注册监听器，此时创建节点附带的数据无法触发数据变更监听器，导致丢一次数据，需要下次才能正确触发
                                            //因此，此处主动读取一次，手工触发事件插件onEvent
                                            plugin.onEvent(getZkClient().readData(parentPath + "/" + p));
                                        }
                                    });
                            vmDataChangeEventPathList.addAll(addList);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("registerVm exception",e);
            throw new RTAException(e);
        }
    }

    /**
     * 递归删除当前vm
     */
    @Override
    public void unregisterVm() {
        if (getZkClient() !=null) {

//            vmDataChangeEventPathList.clear();
//            logger.info("clear dataChangeEventPathList");
            rtaDataChangeEventPathList.clear();
            logger.info("clear rtaDataChangeEventPathList");
            getZkClient().unsubscribeAll();
            logger.info("unsubscribe all listener");

            getZkClient().deleteRecursive(getVmIdPath());
            logger.info("deleteRecursive vm path is {}",getVmIdPath());
        }
    }

    /**
     * 注册RTA公共事件路径监听器，这里的RTADownEventPath具有相对意义，需要相对赋值，所以需要子类来指定
     * 推荐做法：
     *       受控端（Client）RTADownEventPath是指受控端的RTA下行事件路径，也即控制端的RTA上行事件路径，受控端在此路径注册数据变更监听器，监听此路径并接收控制端发布的事件。
     * 相对：
     *       控制端（Server）RTADownEventPath是指RTA下行事件路径，也即受控端的上行事件路径，控制端在此路径注册数据变更监听器，监听此路径并接收受控端发布的事件。
     *
     * @param RTADownEventPath
     */
    public void registerEventListener(String RTADownEventPath) {

        // 注册下行事件ZK路径，用于vm接收事件
        if (getZkClient() != null && !getZkClient().exists(RTADownEventPath)) {
            getZkClient().createPersistent(RTADownEventPath, true);
            logger.info("register listener at RTADownEventPath is {}", RTADownEventPath);
            // 注册对应下行事件的数据变更监听器
            getZkClient().subscribeChildChanges(RTADownEventPath, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    logger.info("ChildChange size is {} and current listener size is {}",currentChilds.size(),getZkClient().numberOfListeners());
                    //currentChilds内即是自定义事件的全限定类名称的列表
                    if (rtaDataChangeEventPathList.size() > 0) {
                        List<String> removelist = new ArrayList<>();
                        // 删除的事件需要解除数据变更监听器
                        rtaDataChangeEventPathList
                                .stream()
                                .filter(p -> !currentChilds.contains(p))
                                .forEach(p -> {
                                    removelist.add(p);
                                    getZkClient().unsubscribeDataChanges(parentPath + "/" + p, EVENT_DATA_CHANGE_LISTENER);
                                    logger.info("unsubscribeDataChange path is {} and current listener size is {}", parentPath + "/" + p,getZkClient().numberOfListeners());
                                });
                        rtaDataChangeEventPathList.removeAll(removelist);
                    }
                    // 新增且有对应注册的事件插件的事件需要添加数据变更监听器
                    if (currentChilds.size() > 0) {
                        List<String> addList = new ArrayList<>();
                        currentChilds
                                .stream()
                                .filter(p -> !rtaDataChangeEventPathList.contains(p))
                                .forEach(p -> {
                                    RTAEventPluginFactory factory = applicationContext.getBean(RTAEventPluginFactory.class);
                                    //获得本地注册的事件插件
                                    RTAEventPlugin plugin = factory.getPluginByEvent(p);
                                    // 如果本地注册了对应事件的插件，则订阅该插件的数据变更事件，如果本地没有注册对应的事件插件，则忽略
                                    if (plugin != null) {
                                        addList.add(p);
                                        getZkClient().subscribeDataChanges(parentPath + "/" + p, EVENT_DATA_CHANGE_LISTENER);
                                        logger.info("subscribeDataChange path is {} and current listener size is {}", parentPath + "/" + p,getZkClient().numberOfListeners());
                                        //如果是第一次创建DownEvent，进而注册监听器，此时创建节点附带的数据无法触发数据变更监听器，导致丢一次数据，需要下次才能正确触发
                                        //因此，此处主动读取一次，手工触发事件插件onEvent
                                        plugin.onEvent(getZkClient().readData(parentPath + "/" + p));
                                    }
                                });
                        rtaDataChangeEventPathList.addAll(addList);
                    }
                }
            });
        }
    }

    /**
     * 注册RTAEvent监听器，由子类根据子类定位调用registerEventListener(String RTADownEventPath)实现
     */
    public abstract void registerEventListener();

    /**
     * 向VM上行事件路径发布事件（写数据）
     * @param object
     */
    @Override
    public void publishVmUpEvent(Object object) {
        if (getZkClient() != null) {
            String path = getVmUpEventPath() + "/" + object.getClass().getName();
            if (!getZkClient().exists(path)) {
                getZkClient().create(path, object, CreateMode.EPHEMERAL);
                logger.info("register VmUpEvent path is {}", path);
            } else {
                getZkClient().writeData(path, object);
                logger.info("publish VmUpEvent and writeData to {}", path);
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
    public void publishRTAUpEvent(String RTAUpEventPath,Object object) {
        if (getZkClient() != null) {
            String path = RTAUpEventPath + "/" + object.getClass().getName();
            if (!getZkClient().exists(path)) {
                getZkClient().create(path, object, CreateMode.EPHEMERAL);
                logger.info("register RTAUpEvent path is {}", path);
            } else {
                getZkClient().writeData(path, object);
                logger.info("publish RTAUpEvent and writeData to {}", path);
            }
        } else {
            logger.warn("zkClient is null and publish event by {} failure",object.getClass().getName());
        }
    }

    /**
     * 发布RTAUpEvent事件方法，由子类实现
     * @param object
     */
    @Override
    public abstract void publishRTAUpEvent(Object object);

    /**
     * 获得app路径
     * @return
     */
    private String getAppPath() {
        return RTAConstant.RTA_ROOT_PATH_NAME + "/" + properties.getApp();
    }

    /**
     * 获得vm注册路径
     * @return
     */
    private String getVmPath() {
        return RTAConstant.RTA_ROOT_PATH_NAME + "/" + properties.getApp() + RTAConstant.VM_PATH_NAME;
    }

    /**
     * 获得vm进程路径
     * @return
     */
    private String getVmIdPath() {
        return getVmPath() + "/" + properties.getPid();
    }

    /**
     * 获得VM下行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getVmDownEventPath() {
        return getVmIdPath() + RTAConstant.VM_DOWNEVENT_PATH_NAME;
    }

    /**
     * 获得VM上行事件路径，该路径下注册有vm，pid做key，事件对象值作为值
     * @return
     */
    public String getVmUpEventPath() {
        return getVmIdPath() + RTAConstant.VM_UPEVENT_PATH_NAME;
    }

    /**
     * 获得RTA公共上行事件路径
     * @return
     */
    public String getRTAUpEventPath() {
        return getAppPath() + RTAConstant.RTA_EVENT_PATH_NAME + RTAConstant.RTA_UPEVENT_PATH_NAME;
    }

    /**
     * 获得RTA公共下行事件路径
     * @return
     */
    public String getRTADownEventPath() {
        return getAppPath() + RTAConstant.RTA_EVENT_PATH_NAME + RTAConstant.RTA_DOWNEVENT_PATH_NAME;
    }
}