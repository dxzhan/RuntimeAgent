package com.resms.rta.common.meta;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * RTA元素据类
 *
 * @author sam
 */
@Data
@Component
@ConfigurationProperties(prefix = "rta")
@Validated
public class RTAMetaProperties {
    @NotNull
    private String app;
    @NotNull
    private String type;
    @NotNull
    private String pid;
    @NotNull
    private String zkAddress;
    private int zkSessionTimeout;
    private int zkConnectionTimeout;
    private int heartDelay;
    private int retry;
    private int retryDuration;

    @NotNull
    private String address;
    @Min(80)
    @Max(65536)
    private int port;
    private String desc;

    private Map<String,String> meta;
}