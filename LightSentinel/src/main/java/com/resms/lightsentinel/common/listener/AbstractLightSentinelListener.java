package com.resms.lightsentinel.common.listener;

import com.resms.lightsentinel.common.LightSentinelBootstrap;
import com.resms.lightsentinel.common.LightSentinelException;
import com.resms.lightsentinel.common.event.AbstractLightSentinelEvent;
import com.resms.lightsentinel.common.handler.LightSentinelEventHandler;
import com.resms.lightsentinel.common.handler.LightSentinelEventRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.*;

import java.io.IOException;

/**
 * LightSentinel事件监听器抽象类
 *
 * @author sam
 */
public abstract class AbstractLightSentinelListener implements ApplicationListener<AbstractLightSentinelEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLightSentinelListener.class);

    /**
     * LightSentinel事件注册中心
     */
    protected LightSentinelEventRegistry registry;
    /**
     * LightSentinel管理器
     */
    protected LightSentinelBootstrap bootstrap;

    public AbstractLightSentinelListener(LightSentinelEventRegistry registry, LightSentinelBootstrap bootstrap) {
        this.registry = registry;
        this.bootstrap = bootstrap;
    }

    @Override
    public void onApplicationEvent(AbstractLightSentinelEvent event) {
        LightSentinelEventHandler plugin = registry.getHandlerByEvent(event.getClass().getName());
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
        try {
            bootstrap.start();
            logger.info("LightSentinelBootstrap started!");
            logger.info("application refreshed!");
        } catch (LightSentinelException e) {
            logger.error("LightSentinelBootstrap start failure!");
        }
    }

    @EventListener
    public void onApplicationStartedEvent(ContextStartedEvent contextStartedEvent) {
        try{
            logger.info("application started!");
            bootstrap.start();
            //contextStartedEvent.getApplicationContext().publishEvent(LivenessEvent.success());
        } catch (LightSentinelException e) {
            logger.error("LightSentinelBootstrap start failure!");
        }
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