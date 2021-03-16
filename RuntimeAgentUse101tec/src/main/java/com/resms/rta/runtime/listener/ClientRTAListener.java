package com.resms.rta.runtime.listener;

import com.resms.rta.runtime.ClientRTAManager;
import com.resms.rta.common.listener.AbstractRTAListener;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RTA事件监听器
 *
 * @author sam
 */
@Component
public class ClientRTAListener extends AbstractRTAListener {
    private static final Logger logger = LoggerFactory.getLogger(ClientRTAListener.class);

    @Autowired
    public ClientRTAListener(RTAEventPluginFactory factory, ClientRTAManager manager) {
        super(factory,manager);
    }
}