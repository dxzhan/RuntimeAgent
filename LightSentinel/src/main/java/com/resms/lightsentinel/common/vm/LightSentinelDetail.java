package com.resms.lightsentinel.common.vm;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * VM详情
 *
 * @author sam
 */
@Data
@EqualsAndHashCode
@Builder
@JsonRootName("VmDetail")
public class LightSentinelDetail
{
    private String app;
    private String type;
    private String nodeId;
    private String address;
    private int port;
    private String description;
}