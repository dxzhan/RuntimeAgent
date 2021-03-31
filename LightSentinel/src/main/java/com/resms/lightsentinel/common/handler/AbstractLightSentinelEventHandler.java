package com.resms.lightsentinel.common.handler;

import com.resms.lightsentinel.common.LightSentinelException;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * LightSentinel事件插件抽象类，自定义事件需要继承此抽象基类
 *
 * @param <EVENT>
 *
 * @author sam
 */
public abstract class AbstractLightSentinelEventHandler<EVENT extends ApplicationEvent> implements LightSentinelEventHandler<EVENT> {

    protected LightSentinelEventRegistry registry;

    protected Class<EVENT> eventType;

    protected String guid;

    public AbstractLightSentinelEventHandler(LightSentinelEventRegistry registry) throws LightSentinelException {
        this.registry = registry;
        initGenericClass();
        if(!mount()) {
            throw new LightSentinelException("Handler already exists!");
        }
    }

    @Override
    public Class<EVENT> getEventType() {
        return eventType;
    }

    @Override
    public String getGuid() {
        return guid == null?this.getClass().getName():guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * 获得泛型事件类型
     */
    private void initGenericClass(){
        try {
            Type parentType = this.getClass().getGenericSuperclass();
            while (!(parentType instanceof ParameterizedType)){
                parentType = ((Class<?>)parentType).getGenericSuperclass();
                if (parentType == null || Object.class.equals(parentType.getClass())) {
                    break;
                }
            }

            if (parentType instanceof ParameterizedType){
                ParameterizedType genericParentType = (ParameterizedType)parentType;
                if (genericParentType.getRawType().equals(AbstractLightSentinelEventHandler.class)){
                    this.eventType = (Class<EVENT>)genericParentType.getActualTypeArguments()[0];
                }
            } else {
                //System.out.println("非直接继承泛型事件基类AbstractLightSentinelEvent");
                if (this.getClass().equals(AbstractLightSentinelEventHandler.class)){
                    this.eventType = (Class<EVENT>)((ParameterizedType)this.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                }
            }
        } catch (Exception e){
            throw new LightSentinelException("generic event type init fail!");
        }
    }

    @Override
    public boolean mount() {
        return registry.mount(this);
    }

    @Override
    public void umount(boolean force) {
        registry.unmount(this);
    }

    @Override
    public String getDesc() {
        return "";
    }

    @Override
    public abstract void onEvent(EVENT event);
}