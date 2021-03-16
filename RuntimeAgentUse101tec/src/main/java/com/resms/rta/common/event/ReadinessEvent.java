package com.resms.rta.common.event;

import com.resms.rta.common.ProbeStatus;

/**
 * 可读性事件
 *
 * @author sam
 */
public class ReadinessEvent extends AbstractRTAEvent<ProbeStatus> {
    public ReadinessEvent(ProbeStatus status) {
        super(status);
    }
}