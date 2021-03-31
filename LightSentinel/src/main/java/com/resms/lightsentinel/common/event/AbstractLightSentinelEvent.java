package com.resms.lightsentinel.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * LightSentinel事件抽象类，自定义LightSentinel事件需要继承此抽象类
 *
 * @param <T>
 *
 * @author sam
 */
public abstract class AbstractLightSentinelEvent<T> extends ApplicationEvent implements LightSentinelEvent {

    public AbstractLightSentinelEvent(T data) {
        super(data);
    }

    @Override
    public T getSource() {
        return (T)super.getSource();
    }
}