package com.resms.lightsentinel.common.handler;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件注册中心类，负责插件的管理
 *
 * @author sam
 */
@Component
public class LightSentinelEventRegistry {
    /**
     *
     */
    private static final Map<String, List<LightSentinelEventHandler>> handlerCache = new ConcurrentHashMap<>();

    /**
     * 根据插件的guid获得事件插件
     * @param guid
     * @param <T>
     * @return
     */
    public <T extends LightSentinelEventHandler> T getHandlerByGUID(String guid) {
        return (T) handlerCache.get(guid);
    }

    /**
     * 根据事件类型名（类全限定名）获得事件插件
     * @param eventTypeName
     * @param <T>
     * @return
     */
    public <T extends LightSentinelEventHandler> T getHandlerByEvent(String eventTypeName) {
        if (handlerCache.containsKey(eventTypeName)) {
            return (T) handlerCache.get(eventTypeName).stream().filter(e->e.getEventType().getName().equals(eventTypeName)).findFirst().get();
        }
        return null;
    }

    /**
     * 挂载事件处理器
     * @param handler
     * @return
     */
    public boolean mount(LightSentinelEventHandler handler) {
        if (!handlerCache.containsKey(handler.getEventType().getName())) {
            List<LightSentinelEventHandler> handlers = new ArrayList<>();
            handlers.add(handler);
            handlerCache.put(handler.getEventType().getName(),handlers);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 卸载事件处理器
     * @param handler
     */
    public void unmount(LightSentinelEventHandler handler) {
        if (handlerCache.containsKey(handler.getEventType().getName())) {
            if (handlerCache.get(handler.getEventType().getName()).contains(handler)) {
                handlerCache.get(handler.getEventType().getName()).remove(handler);
            }
        }
    }
}