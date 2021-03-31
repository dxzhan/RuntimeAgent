package com.resms.lightsentinel.runtime.handler;

import com.resms.lightsentinel.runtime.LightSentinelRuntimeBootstrap;
import com.resms.lightsentinel.common.handler.AbstractLightSentinelEventHandler;
import com.resms.lightsentinel.common.handler.LightSentinelEventRegistry;
import com.resms.lightsentinel.common.LightSentinelException;
import com.resms.lightsentinel.common.event.DeployEvent;
import com.resms.lightsentinel.common.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 部署事件插件
 *
 * @author sam
 */
@Component
public class DeployEventHandler extends AbstractLightSentinelEventHandler<DeployEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DeployEventHandler.class);
    private LightSentinelRuntimeBootstrap bootstrap;

    @Autowired
    public DeployEventHandler(LightSentinelEventRegistry registry, LightSentinelRuntimeBootstrap bootstrap) throws LightSentinelException {
        super(registry);
        this.bootstrap = bootstrap;
    }

    @Override
    public void onEvent(DeployEvent event) {
        logger.info("DeployEventHandler: deploy event trigger and event is " + JsonMapper.toJson(event));
    }

    public LightSentinelRuntimeBootstrap getBootstrap() {
        return bootstrap;
    }
}