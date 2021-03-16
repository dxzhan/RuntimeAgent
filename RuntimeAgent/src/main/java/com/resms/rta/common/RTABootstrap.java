package com.resms.rta.common;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationContext;

import java.io.Closeable;

/**
 * RTABootstrap接口
 *
 * @author sam
 */
public interface RTABootstrap extends Closeable {
    ApplicationContext getApplicationContext();

    void start();

    void afterStart();

    boolean isRunning();

    CuratorFramework getZkClient();

    void publishVmEvent(String basePath,Object object);

    void publishRTAEvent(String RTAUpEventPath, Object object);
}