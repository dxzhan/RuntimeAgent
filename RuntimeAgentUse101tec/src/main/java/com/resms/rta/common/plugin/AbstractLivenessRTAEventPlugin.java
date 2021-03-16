package com.resms.rta.common.plugin;

import com.resms.rta.common.ProbeStatus;
import com.resms.rta.common.RTAException;
import com.resms.rta.common.RTAManager;
import com.resms.rta.common.event.LivenessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 活跃度事件插件
 *
 * @author sam
 */
public abstract class AbstractLivenessRTAEventPlugin extends AbstractRTAEventPlugin<LivenessEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLivenessRTAEventPlugin.class);
    protected RTAManager manager;

    public AbstractLivenessRTAEventPlugin(RTAEventPluginFactory factory, RTAManager manager) throws RTAException {
        super(factory);
        this.manager = manager;
    }

    @Override
    public void onEvent(LivenessEvent event) {
        logger.info("app liveness probe is " + event.getSource().name());
        if (ProbeStatus.SUCCESS.equals(event.getSource())) {
            manager.registerVm();
        }
        else if (ProbeStatus.FAILURE.equals(event.getSource())) {
            manager.unregisterVm();
        }
    }
}