package com.resms.rta;

import com.resms.rta.runtime.ClientRTAManager;
import com.resms.rta.common.event.DeployEvent;
import com.resms.rta.common.meta.RTAMetaProperties;
import com.resms.rta.common.util.JsonMapper;
import org.I0Itec.zkclient.ZkClient;
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
    private ClientRTAManager manager;

    @GetMapping("/deploy")
    public String publishDeployDownEvent() {
        DeployEvent event = new DeployEvent("");
        event.setFtpAddress("127.0.0.1");
        event.setFtpPort("22");
        event.setFtpUserName("sam");
        event.setFtpPassword(System.currentTimeMillis() + "");
        event.setImageName("xxxx.zip");
        event.setImagePath("/nms");

        String deployDownEventPath = manager.getVmDownEventPath() + "/" + DeployEvent.class.getName();
        ZkClient zkClient = new ZkClient(properties.getZkAddress(), Integer.MAX_VALUE, properties.getZkConnectionTimeout());
        if (!zkClient.exists(deployDownEventPath)) {
            zkClient.createPersistent(deployDownEventPath,true);
            //zkClient.writeData(deployDownEventPath, event);
            logger.info("register DeployDownEvent path is {}",deployDownEventPath);
        }
        zkClient.writeData(deployDownEventPath, event);
        logger.info("publish DeployDownEvent and writeData path is {}",deployDownEventPath);

        zkClient.close();
        return JsonMapper.toJson(event);
    }

    @GetMapping("/deldeploy")
    public String delDeployDownEvent() {
        String deployDownEventPath = manager.getVmDownEventPath() + "/" + DeployEvent.class.getName();
        ZkClient zkClient = new ZkClient(properties.getZkAddress(), Integer.MAX_VALUE, properties.getZkConnectionTimeout());
        boolean isDeleted = zkClient.delete(deployDownEventPath);
        logger.info("delete DeployDownEvent path is {} and isDeleted is {}",deployDownEventPath,isDeleted);
        zkClient.close();
        return "success";
    }
}