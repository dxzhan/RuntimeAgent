package com.resms.rta.common.plugin;

import com.resms.rta.common.RTAException;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * RTA事件插件抽象类，自定义事件需要继承此抽象基类
 *
 * @param <EVENT>
 *
 * @author sam
 */
public abstract class AbstractRTAEventPlugin<EVENT extends ApplicationEvent> implements RTAEventPlugin<EVENT> {

    protected RTAEventPluginFactory factory;

    protected Class<EVENT> eventType;

    protected String guid;

    public AbstractRTAEventPlugin(RTAEventPluginFactory factory) throws RTAException {
        this.factory = factory;
        initGenericClass();
        if(!mount()) {
            throw new RTAException("Plugin already exists!");
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
                if (genericParentType.getRawType().equals(AbstractRTAEventPlugin.class)){
                    this.eventType = (Class<EVENT>)genericParentType.getActualTypeArguments()[0];
                }
            } else {
                //System.out.println("非直接继承泛型事件基类AbstractRTAEvent");
                if (this.getClass().equals(AbstractRTAEventPlugin.class)){
                    this.eventType = (Class<EVENT>)((ParameterizedType)this.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                }
            }
        } catch (Exception e){
            throw new RTAException("generic event type init fail!");
        }
    }

    @Override
    public boolean mount() {
        return factory.mount(this);
    }

    @Override
    public void umount(boolean force) {
        factory.unmount(this);
    }

    @Override
    public String getDesc() {
        return "";
    }

    @Override
    public abstract void onEvent(EVENT event);
}