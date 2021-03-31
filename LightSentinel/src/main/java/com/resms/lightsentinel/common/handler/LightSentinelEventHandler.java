package com.resms.lightsentinel.common.handler;

import org.springframework.context.ApplicationEvent;

/**
 * LightSentinel事件处理器接口
 *
 * @param <EVENT>
 *
 * @author sam
 */
public interface LightSentinelEventHandler<EVENT extends ApplicationEvent> {
    /**
     * 事件处理插件唯一标识，目前是插件类全限定名称
     * @return
     */
    String getGuid();

    /**
     * 事件处理插件描述信息
     * @return
     */
    String getDesc();

    /**
     * 当前事件插件处理的事件类型
     * @return
     */
    Class<EVENT> getEventType();

    /**
     * 插件挂载
     */
    boolean mount();

    /**
     * 插件卸载
     * @param force
     */
    void umount(boolean force);

    /**
     * 事件处理逻辑的异常会被吞掉，只打印异常信息
     * @param event
     */
    void onEvent(EVENT event);
}