package com.resms.rta.manager;

import com.resms.rta.common.AbstractRTABootstrap;
import com.resms.rta.common.RTABootstrap;
import com.resms.rta.common.meta.RTAMetaProperties;
import com.resms.rta.common.vm.RTAVmDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * RTA管理器
 *
 * @author sam
 */
@Component
public class RTAManagerBootstrap extends AbstractRTABootstrap implements RTABootstrap {
    private static final Logger logger = LoggerFactory.getLogger(RTAManagerBootstrap.class);
    private RTAVmDiscovery discovery;

    @Autowired
    public RTAManagerBootstrap(ApplicationContext applicationContext, RTAMetaProperties properties) {
        super(applicationContext,properties);
        discovery = new RTAVmDiscovery(zkClient, getRVmPath(),properties);
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
        registerEventListener(getRTAUpEventPath());
    }

    /**
     * 向event事件路径发布事件（写数据）
     * @param object
     */
    public void publishRTAEvent(Object object) {
        publishRTAEvent(getRTADownEventPath(),object);
    }

    public RTAVmDiscovery getDiscovery() {
        return discovery;
    }
}