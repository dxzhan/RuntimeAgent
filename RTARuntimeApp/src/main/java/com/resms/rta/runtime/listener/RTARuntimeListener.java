package com.resms.rta.runtime.listener;

import com.resms.rta.runtime.RTARuntimeBootstrap;
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
public class RTARuntimeListener extends AbstractRTAListener {
    private static final Logger logger = LoggerFactory.getLogger(RTARuntimeListener.class);

    @Autowired
    public RTARuntimeListener(RTAEventPluginFactory factory, RTARuntimeBootstrap manager) {
        super(factory,manager);
    }
}