package com.resms.rta;

import com.resms.rta.common.RTAConstant;
import com.resms.rta.common.event.DeployEvent;
import com.resms.rta.common.meta.RTAMetaProperties;
import com.resms.rta.common.util.JsonMapper;
import com.resms.rta.manager.RTAManagerBootstrap;
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
    private RTAManagerBootstrap bootstrap;

    @GetMapping("/deploy")
    public String publishDeployDownEvent() {
        DeployEvent event = new DeployEvent("Manager");
        event.setFtpAddress("127.0.0.1");
        event.setFtpPort("22");
        event.setFtpUserName("sam");
        event.setFtpPassword(System.currentTimeMillis() + "");
        event.setImageName("xxxx.zip");
        event.setImagePath("/nms");
        // 通过每个runtime的下行事件路径推送
        bootstrap.getDiscovery().getVmNameList().stream().forEach(s->{
            String deployVmDownEventPath = bootstrap.getRVmPath() + "/" + s + RTAConstant.VM_DOWNEVENT_PATH_NAME + "/" + DeployEvent.class.getName();
            try {
                if (bootstrap.getZkClient().checkExists().forPath(deployVmDownEventPath) == null) {
                    bootstrap.getZkClient().create().withMode(CreateMode.EPHEMERAL).forPath(deployVmDownEventPath, JsonMapper.toJsonBytes(event));
                    //zkClient.writeData(deployDownEventPath, event);
                    logger.info("register DeployVmDownEvent and writeData path is {}", deployVmDownEventPath);
                } else {
                    bootstrap.getZkClient().setData().forPath(deployVmDownEventPath, JsonMapper.toJsonBytes(event));
                    logger.info("publish DeployVmDownEvent and writeData path is {}", deployVmDownEventPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return JsonMapper.toJson(event);
    }

    @GetMapping("/deldeploy")
    public String delDeployDownEvent() {

        bootstrap.getDiscovery().getVmNameList().stream().forEach(s->{
            String deployVmDownEventPath = bootstrap.getRVmPath() + "/" + s + RTAConstant.VM_DOWNEVENT_PATH_NAME + "/" + DeployEvent.class.getName();
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
        DeployEvent event = new DeployEvent("Manager");
        event.setFtpAddress("192.168.66.88");
        event.setFtpPort("22");
        event.setFtpUserName("sam");
        event.setFtpPassword(System.currentTimeMillis() + "");
        event.setImageName("xxxx.zip");
        event.setImagePath("/nms");

        bootstrap.publishRTAEvent(event);

        return JsonMapper.toJson(event);
    }
}