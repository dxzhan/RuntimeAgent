package com.resms.lightsentinel.manager;

import com.resms.lightsentinel.common.AbstractLightSentinelBootstrap;
import com.resms.lightsentinel.common.LightSentinelBootstrap;
import com.resms.lightsentinel.common.LightSentinelConstant;
import com.resms.lightsentinel.common.event.AbstractLightSentinelEvent;
import com.resms.lightsentinel.common.meta.LightSentinelMetaProperties;
import com.resms.lightsentinel.common.vm.LightSentinelVmDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * LightSentinel管理器
 *
 * @author sam
 */
@Component
public class LightSentinelManagerBootstrap extends AbstractLightSentinelBootstrap implements LightSentinelBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelManagerBootstrap.class);
    private LightSentinelVmDiscovery discovery;

    @Autowired
    public LightSentinelManagerBootstrap(ApplicationContext applicationContext, LightSentinelMetaProperties properties) {
        super(applicationContext,properties);
        discovery = new LightSentinelVmDiscovery(zkClient, getRVmPath(),properties);
    }

    @Override
    public void afterStart() {
        discovery.start();

        registerEventListener();
    }

    @Override
    public void close() {
        super.close();
        discovery.close();
    }

    @Override
    public void registerEventListener() {
        registerEventListener(getLightSentinelUpEventPath());
    }

    /**
     * 向event事件路径发布事件（写数据）
     * @param object
     */
    public void publishLightSentinelEvent(Object object) {
        publishLightSentinelEvent(getLightSentinelDownEventPath(),object);
    }

    /**
     * 通过每个runtime的下行事件路径推送事件
     * @param event
     */
    public void brodcastVmDownEvent(AbstractLightSentinelEvent event) {
        getDiscovery().getVmNameList().stream().forEach(s->{
            String vmDownEventPath = getRVmPath() + "/" + s + LightSentinelConstant.VM_DOWNEVENT_PATH_NAME;
            try {
                publishVmEvent(vmDownEventPath,event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public LightSentinelVmDiscovery getDiscovery() {
        return discovery;
    }
}