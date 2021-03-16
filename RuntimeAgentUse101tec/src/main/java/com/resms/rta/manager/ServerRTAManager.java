package com.resms.rta.manager;

import com.resms.rta.common.AbstractRTAManager;
import com.resms.rta.common.RTAManager;
import com.resms.rta.common.meta.RTAMetaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * RTA管理器
 *
 * @author sam
 */
@Component
public class ServerRTAManager extends AbstractRTAManager implements RTAManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerRTAManager.class);

    @Autowired
    public ServerRTAManager(ApplicationContext applicationContext, RTAMetaProperties properties) {
        super(applicationContext,properties);
    }

    @Override
    public void registerEventListener() {
        registerEventListener(getRTAUpEventPath());
    }

    /**
     * 向VM上行事件路径发布事件（写数据）
     * @param object
     */
    @Override
    public void publishRTAUpEvent(Object object) {
        publishRTAUpEvent(getRTADownEventPath(),object);
    }
}