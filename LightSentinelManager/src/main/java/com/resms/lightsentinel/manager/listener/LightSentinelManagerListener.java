package com.resms.lightsentinel.manager.listener;

import com.resms.lightsentinel.manager.LightSentinelManagerBootstrap;
import com.resms.lightsentinel.common.listener.AbstractLightSentinelListener;
import com.resms.lightsentinel.common.handler.LightSentinelEventRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * LightSentinel事件监听器
 *
 * @author sam
 */
@Component
public class LightSentinelManagerListener extends AbstractLightSentinelListener {
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelManagerListener.class);

    @Autowired
    public LightSentinelManagerListener(LightSentinelEventRegistry factory, LightSentinelManagerBootstrap bootstrap) {
        super(factory,bootstrap);
    }


}