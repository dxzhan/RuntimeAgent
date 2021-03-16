package com.resms.rta.runtime.plugin;

import com.resms.rta.runtime.ClientRTAManager;
import com.resms.rta.common.plugin.AbstractRTAEventPlugin;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
import com.resms.rta.common.RTAException;
import com.resms.rta.common.event.DeployEvent;
import com.resms.rta.common.util.JsonMapper;
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
public class ClientDeployRTAEventPlugin extends AbstractRTAEventPlugin<DeployEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ClientDeployRTAEventPlugin.class);
    private ClientRTAManager manager;

    @Autowired
    public ClientDeployRTAEventPlugin(RTAEventPluginFactory factory, ClientRTAManager manager) throws RTAException {
        super(factory);
        this.manager = manager;
    }

    @Override
    public void onEvent(DeployEvent event) {
        logger.info("DeployRTAEventPlugin: deploy event trigger and event is " + JsonMapper.toJson(event));
    }

    public ClientRTAManager getManager() {
        return manager;
    }
}