package com.resms.rta.runtime.plugin;

import com.resms.rta.runtime.ClientRTAManager;
import com.resms.rta.common.RTAException;
import com.resms.rta.common.plugin.AbstractLivenessRTAEventPlugin;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
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
public class ClientLivenessRTAEventPlugin extends AbstractLivenessRTAEventPlugin {
    private static final Logger logger = LoggerFactory.getLogger(ClientLivenessRTAEventPlugin.class);

    @Autowired
    public ClientLivenessRTAEventPlugin(RTAEventPluginFactory factory, ClientRTAManager manager) throws RTAException {
        super(factory,manager);
        this.manager = manager;
    }
}