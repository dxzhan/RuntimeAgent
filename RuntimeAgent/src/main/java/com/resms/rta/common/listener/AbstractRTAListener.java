package com.resms.rta.common.listener;

import com.resms.rta.common.RTABootstrap;
import com.resms.rta.common.event.AbstractRTAEvent;
import com.resms.rta.common.plugin.AbstractRTAEventPlugin;
import com.resms.rta.common.plugin.RTAEventPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.*;

import java.io.IOException;

/**
 * RTA事件监听器
 *
 * @author sam
 */
public abstract class AbstractRTAListener implements ApplicationListener<AbstractRTAEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRTAListener.class);

    /**
     * RTA事件插件工厂
     */
    protected RTAEventPluginFactory factory;
    /**
     * RTA管理器
     */
    protected RTABootstrap bootstrap;

    public AbstractRTAListener(RTAEventPluginFactory factory, RTABootstrap bootstrap) {
        this.factory = factory;
        this.bootstrap = bootstrap;
    }

    @Override
    public void onApplicationEvent(AbstractRTAEvent event) {
        AbstractRTAEventPlugin plugin = factory.getPluginByEvent(event.getClass().getName());
        if (plugin != null) {
            try {
                plugin.onEvent(event);
            } catch (Exception e) {
                //吞掉事件处理插件的异常，只打印异常信息
                logger.error("plugin {} exception",plugin.getGuid(),e);
            }
        } else {
            logger.debug("miss event {} hand plugin",event.getClass().getName());
        }
    }

    @EventListener
    public void onApplicationRefreshedEvent(ContextRefreshedEvent contextRefreshedEvent) {
        bootstrap.start();
        logger.info("RTABootstrap started!");
        logger.info("application refreshed!");
    }

    @EventListener
    public void onApplicationStartedEvent(ContextStartedEvent contextStartedEvent) {
        logger.info("application started!");
        bootstrap.start();
        //contextStartedEvent.getApplicationContext().publishEvent(LivenessEvent.success());
    }

    @EventListener
    public void onApplicationStoppedEvent(ContextStoppedEvent contextStoppedEvent) throws IOException {
        logger.info("application stopped!");
        //contextStoppedEvent.getApplicationContext().publishEvent(LivenessEvent.failure());
    }

    @EventListener
    public void onApplicationClosedEvent(ContextClosedEvent contextClosedEvent) throws IOException {
        logger.info("application closed!");
        //contextClosedEvent.getApplicationContext().publishEvent(LivenessEvent.failure());
    }
}