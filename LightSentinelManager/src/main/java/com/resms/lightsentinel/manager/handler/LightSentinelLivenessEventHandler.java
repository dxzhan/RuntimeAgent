package com.resms.lightsentinel.manager.handler;

import com.resms.lightsentinel.common.LightSentinelBootstrap;
import com.resms.lightsentinel.common.LightSentinelException;
import com.resms.lightsentinel.common.event.LivenessEvent;
import com.resms.lightsentinel.common.handler.AbstractLightSentinelEventHandler;
import com.resms.lightsentinel.common.handler.LightSentinelEventRegistry;
import com.resms.lightsentinel.common.util.JsonMapper;
import com.resms.lightsentinel.manager.LightSentinelManagerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 活跃度事件插件
 *
 * @author sam
 */
@Component
public class LightSentinelLivenessEventHandler extends AbstractLightSentinelEventHandler<LivenessEvent> {
    private static final Logger logger = LoggerFactory.getLogger(LightSentinelLivenessEventHandler.class);
    protected LightSentinelBootstrap bootstrap;
    @Autowired
    public LightSentinelLivenessEventHandler(LightSentinelEventRegistry factory, LightSentinelManagerBootstrap bootstrap) throws LightSentinelException {
        super(factory);
        this.bootstrap = bootstrap;
    }

    @Override
    public void onEvent(LivenessEvent event) {
        logger.info("LightSentinelRruntime liveness is " + JsonMapper.toJson(event));
//        if (ProbeStatus.SUCCESS.equals(event.getSource())) {
//        }
//        else if (ProbeStatus.FAILURE.equals(event.getSource())) {
//
//        }
    }
}