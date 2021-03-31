package com.resms.lightsentinel.common;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationContext;

import java.io.Closeable;

/**
 * LightSentinelBootstrap接口
 *
 * @author sam
 */
public interface LightSentinelBootstrap extends Closeable {
    ApplicationContext getApplicationContext();

    void start();

    void afterStart();

    boolean isRunning();

    CuratorFramework getZkClient();

    void publishVmEvent(String basePath,Object object);

    void publishLightSentinelEvent(String LightSentinelUpEventPath, Object object);
}