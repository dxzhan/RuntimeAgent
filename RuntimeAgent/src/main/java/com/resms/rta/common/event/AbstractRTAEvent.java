package com.resms.rta.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * RTA事件抽象类，自定义RTA事件需要继承此抽象类
 *
 * @param <T>
 *
 * @author sam
 */
public abstract class AbstractRTAEvent<T> extends ApplicationEvent implements RTAEvent {

    public AbstractRTAEvent(T data) {
        super(data);
    }

    @Override
    public T getSource() {
        return (T)super.getSource();
    }
}