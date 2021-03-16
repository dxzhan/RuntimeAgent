package com.resms.rta.manager.listener;

import com.resms.rta.manager.RTAManagerBootstrap;
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
public class RTAManagerListener extends AbstractRTAListener {
    private static final Logger logger = LoggerFactory.getLogger(RTAManagerListener.class);

    @Autowired
    public RTAManagerListener(RTAEventPluginFactory factory, RTAManagerBootstrap bootstrap) {
        super(factory,bootstrap);
    }


}