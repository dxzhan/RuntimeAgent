package com.resms.rta.common;

public class RTAConstant {
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final int CORE_POOL_SIZE = Math.max(4,Math.min(AVAILABLE_PROCESSORS - 1, 5));
    public static final int MAX_POOL_SIZE = AVAILABLE_PROCESSORS * 2 + 2;

    /**
     * RTA根路径
     */
    public static final String RTA_ROOT_PATH_NAME = "/rta";
    /**
     * vm路径，其下挂载多个vm
     */
    public static final String VM_PATH_NAME = "/vm";

    /**
     * RTA事件路径，其下挂载down和up
     */
    public static final String RTA_EVENT_PATH_NAME = "/event";

    /**
     * RTA公共上行事件路径
     */
    public static final String RTA_UPEVENT_PATH_NAME = "/up";

    /**
     * RTA公共下行事件路径
     */
    public static final String RTA_DOWNEVENT_PATH_NAME = "/down";

    /**
     * 下行事件路径，用于vm接收和监听下行事件，其下可以注册自定义事件，同时扩展自定义事件插件完成自定义事件处理
     */
    public static final String VM_DOWNEVENT_PATH_NAME = "/downevent";
    /**
     * 上行事件路径，用于vm推送上行事件，其下可以发布自定义事件
     */
    public static final String VM_UPEVENT_PATH_NAME = "/upevent";
}