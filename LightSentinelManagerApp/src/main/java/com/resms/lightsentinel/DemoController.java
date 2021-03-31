package com.resms.lightsentinel;

import com.resms.lightsentinel.common.LightSentinelConstant;
import com.resms.lightsentinel.common.event.DeployEvent;
import com.resms.lightsentinel.common.util.JsonMapper;
import com.resms.lightsentinel.manager.LightSentinelManagerBootstrap;
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
    private LightSentinelManagerBootstrap bootstrap;

    @GetMapping("/deploy")
    public String publishDeployDownEvent() {
        DeployEvent event = new DeployEvent();
        event.setFtpAddress("127.0.0.1");
        event.setFtpPort("22");
        event.setFtpUserName("sam");
        event.setFtpPassword(System.currentTimeMillis() + "");
        event.setImageName("xxxx.zip");
        event.setImagePath("/nms");

        // 通过每个runtime的下行事件路径推送
        bootstrap.brodcastVmDownEvent(event);
        return JsonMapper.toJson(event);
    }

    @GetMapping("/deldeploy")
    public String delDeployDownEvent() {

        bootstrap.getDiscovery().getVmNameList().stream().forEach(s->{
            String deployVmDownEventPath = bootstrap.getRVmPath() + "/" + s + LightSentinelConstant.VM_DOWNEVENT_PATH_NAME + "/" + DeployEvent.class.getName();
            try {
                bootstrap.getZkClient().delete().forPath(deployVmDownEventPath);
                logger.info("delete DeployVmDownEvent path is {} and isDeleted is {}",deployVmDownEventPath,true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return "success";
    }

    @GetMapping("/event/deploy")
    public String publishEventDeployDownEvent() throws Exception {
        DeployEvent event = new DeployEvent();
        event.setFtpAddress("192.168.66.88");
        event.setFtpPort("22");
        event.setFtpUserName("sam");
        event.setFtpPassword(System.currentTimeMillis() + "");
        event.setImageName("xxxx.zip");
        event.setImagePath("/nms");

        bootstrap.publishLightSentinelEvent(event);

        return JsonMapper.toJson(event);
    }
}