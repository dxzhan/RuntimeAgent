package com.resms.lightsentinel.common.service;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.ToString;

/**
 * 服务详情
 *
 * @author sam
 */
@Builder
@ToString
@JsonRootName("ServiceInstanceDetail")
public class ServiceInstanceDetail {
    private String id;
    private String address;
    private String port;
    private String interfaceName;
}