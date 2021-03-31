package com.resms.lightsentinel.common.event;

import lombok.Data;

/**
 * 部署事件
 *
 * @author sam
 */
@Data
public class DeployEvent extends AbstractLightSentinelEvent<String> {
    private String ftpAddress;
    private String ftpPort;
    private String ftpUserName;
    private String ftpPassword;
    private String imagePath;
    private String imageName;

    public DeployEvent() {
        super("DeployEvent");
    }
}