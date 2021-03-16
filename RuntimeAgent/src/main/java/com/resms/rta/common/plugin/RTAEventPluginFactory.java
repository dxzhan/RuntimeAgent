package com.resms.rta.common.plugin;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件插件工厂类，负责插件的管理
 *
 * @author sam
 */
@Component
public class RTAEventPluginFactory {
    private static final Map<String, RTAEventPlugin> pluginCache = new ConcurrentHashMap<>();
    private static final Map<String,String> eventPluginMappingCache = new ConcurrentHashMap<>();

    /**
     * 根据插件的guid获得事件插件
     * @param guid
     * @param <T>
     * @return
     */
    public <T extends RTAEventPlugin> T getPluginByGUID(String guid) {
        return (T) pluginCache.get(guid);
    }

    /**
     * 根据事件类型名（类全限定名）获得事件插件
     * @param eventTypeName
     * @param <T>
     * @return
     */
    public <T extends RTAEventPlugin> T getPluginByEvent(String eventTypeName) {
        if (eventPluginMappingCache.containsKey(eventTypeName)) {
            return (T) pluginCache.get(eventPluginMappingCache.get(eventTypeName));
        }
        return null;
    }

    /**
     * 挂载事件插件
     * @param plugin
     * @return
     */
    public boolean mount(RTAEventPlugin plugin) {
        if (!pluginCache.containsKey(plugin.getGuid()) && !eventPluginMappingCache.containsKey(plugin.getEventType().getName())) {
            pluginCache.put(plugin.getGuid(),plugin);
            eventPluginMappingCache.put(plugin.getEventType().getName(),plugin.getGuid());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 卸载事件插件
     * @param plugin
     */
    public void unmount(RTAEventPlugin plugin) {
        if (pluginCache.containsKey(plugin.getGuid())) {
            pluginCache.remove(plugin.getGuid());
            eventPluginMappingCache.remove(plugin.getEventType().getName());
        }
    }
}