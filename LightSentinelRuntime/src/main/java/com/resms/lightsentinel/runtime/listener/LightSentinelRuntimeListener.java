package com.resms.lightsentinel.runtime.listener;

import com.resms.lightsentinel.runtime.LightSentinelRuntimeBootstrap;
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
public class LightSentinelRuntimeListener extends AbstractLightSentinelListener {
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelRuntimeListener.class);

    @Autowired
    public LightSentinelRuntimeListener(LightSentinelEventRegistry registry, LightSentinelRuntimeBootstrap manager) {
        super(registry,manager);
    }
}