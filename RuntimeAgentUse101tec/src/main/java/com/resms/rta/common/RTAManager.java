package com.resms.rta.common;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.ApplicationContext;

public interface RTAManager {
    ApplicationContext getApplicationContext();

    void start() throws RTAException;

    ZkClient getZkClient();

    void registerVm();

    void unregisterVm();

    void publishVmUpEvent(Object object);

    void publishRTAUpEvent(Object object);
}