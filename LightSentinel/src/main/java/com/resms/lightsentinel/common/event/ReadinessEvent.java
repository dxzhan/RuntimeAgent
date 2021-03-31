package com.resms.lightsentinel.common.event;

import com.resms.lightsentinel.common.meta.ProbeStatus;

/**
 * 可读性事件
 *
 * @author sam
 */
public class ReadinessEvent extends AbstractLightSentinelEvent<String> {
    private ProbeStatus status;
    public ReadinessEvent() {
        this(ProbeStatus.UNKNOWN);
    }
    public ReadinessEvent(ProbeStatus status) {
        super("ReadinessEvent");
        this.status = status;
    }
}