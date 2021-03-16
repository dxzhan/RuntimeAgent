package com.resms.rta;

import com.resms.rta.common.event.LivenessEvent;
import com.resms.rta.common.meta.RTAMetaProperties;
import com.resms.rta.common.util.JsonMapper;
import com.resms.rta.runtime.RTARuntimeBootstrap;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    @Autowired
    private RTAMetaProperties properties;
    @Autowired
    private RTARuntimeBootstrap bootstrap;

    @GetMapping("/liveness")
    public String publishLivenessVmUpEvent() throws Exception {

        String livenessVmUpEventPath = bootstrap.getRVmUpEventPath() + "/" + LivenessEvent.class.getName();

        LivenessEvent event = LivenessEvent.success(properties.getPid());
        if (bootstrap.getZkClient().checkExists().forPath(livenessVmUpEventPath) == null) {
            bootstrap.getZkClient().create().withMode(CreateMode.EPHEMERAL).forPath(livenessVmUpEventPath, JsonMapper.toJsonBytes(event));
            //zkClient.writeData(deployDownEventPath, event);
            logger.info("register LivenessVmUpEvent path is {}",livenessVmUpEventPath);
        } else {
            bootstrap.getZkClient().setData().forPath(livenessVmUpEventPath, JsonMapper.toJsonBytes(event));
            logger.info("publish LivenessVmUpEvent and writeData path is {}", livenessVmUpEventPath);
        }

        return JsonMapper.toJson(event);
    }

    @GetMapping("/delliveness")
    public String delLivenessVMUpEvent() throws Exception {
        String livenessVmUpEventPath = bootstrap.getRVmUpEventPath() + "/" + LivenessEvent.class.getName();
        bootstrap.getZkClient().delete().forPath(livenessVmUpEventPath);
        logger.info("delete LivenessVmUpEvent path is {} and isDeleted is {}",livenessVmUpEventPath,true);
        return "success";
    }

    @GetMapping("/event/liveness")
    public String publishLivenessRTAUpEvent() throws Exception {
        LivenessEvent event = LivenessEvent.success(properties.getPid());

        String livenessRTAUpEventPath = bootstrap.getRTAUpEventPath() + "/" + LivenessEvent.class.getName();

        if (bootstrap.getZkClient().checkExists().forPath(livenessRTAUpEventPath) == null) {
            bootstrap.getZkClient().create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(livenessRTAUpEventPath, JsonMapper.toJsonBytes(event));
            //zkClient.writeData(deployDownEventPath, event);
            logger.info("register LivenessRTAUpEvent path is {}",livenessRTAUpEventPath);
        } else {
            bootstrap.getZkClient().setData().forPath(livenessRTAUpEventPath, JsonMapper.toJsonBytes(event));
            logger.info("publish LivenessRTAUpEvent and writeData path is {}", livenessRTAUpEventPath);
        }
        return JsonMapper.toJson(event);
    }
}