package com.resms.lightsentinel;

import com.resms.lightsentinel.common.event.LivenessEvent;
import com.resms.lightsentinel.common.meta.LightSentinelMetaProperties;
import com.resms.lightsentinel.common.util.JsonMapper;
import com.resms.lightsentinel.runtime.LightSentinelRuntimeBootstrap;
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
    private LightSentinelMetaProperties properties;
    @Autowired
    private LightSentinelRuntimeBootstrap bootstrap;

    @GetMapping("/liveness")
    public String publishLivenessVmUpEvent() throws Exception {
        LivenessEvent event = LivenessEvent.success(properties.getNodeId());
        bootstrap.publishVmEvent(event);

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
    public String publishLightSentinelLivenessUpEvent() throws Exception {
        LivenessEvent event = LivenessEvent.success(properties.getNodeId());
        bootstrap.publishLightSentinelEvent(event);
        return JsonMapper.toJson(event);
    }
}