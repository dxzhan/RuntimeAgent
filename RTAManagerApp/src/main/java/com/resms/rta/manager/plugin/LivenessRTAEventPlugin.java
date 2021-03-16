package com.resms.rta.manager.plugin;

import com.resms.rta.common.RTABootstrap;
import com.resms.rta.common.RTAException;
import com.resms.rta.common.event.LivenessEvent;
import com.resms.rta.common.plugin.AbstractRTAEventPlugin;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
import com.resms.rta.common.util.JsonMapper;
import com.resms.rta.manager.RTAManagerBootstrap;
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
public class LivenessRTAEventPlugin extends AbstractRTAEventPlugin<LivenessEvent> {
    private static final Logger logger = LoggerFactory.getLogger(LivenessRTAEventPlugin.class);
    protected RTABootstrap bootstrap;
    @Autowired
    public LivenessRTAEventPlugin(RTAEventPluginFactory factory, RTAManagerBootstrap bootstrap) throws RTAException {
        super(factory);
        this.bootstrap = bootstrap;
    }

    @Override
    public void onEvent(LivenessEvent event) {
        logger.info("RTARruntime liveness is " + JsonMapper.toJson(event));
//        if (ProbeStatus.SUCCESS.equals(event.getSource())) {
//        }
//        else if (ProbeStatus.FAILURE.equals(event.getSource())) {
//
//        }
    }
}