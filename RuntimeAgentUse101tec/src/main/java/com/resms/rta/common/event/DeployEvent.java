package com.resms.rta.common.event;

import lombok.Data;

@Data
public class DeployEvent extends AbstractRTAEvent<Object> {
    private String ftpAddress;
    private String ftpPort;
    private String ftpUserName;
    private String ftpPassword;
    private String imagePath;
    private String imageName;

    public DeployEvent(Object object) {
        super(object);
    }
}