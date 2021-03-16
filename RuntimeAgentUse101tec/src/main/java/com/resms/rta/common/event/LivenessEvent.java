package com.resms.rta.common.event;

import com.resms.rta.common.ProbeStatus;

/**
 * 活跃度事件
 *
 * @author sam
 */
public class LivenessEvent extends AbstractRTAEvent<ProbeStatus> {
    private static final long serialVersionUID = 1L;

    public LivenessEvent(ProbeStatus status) {
        super(status);
    }

    public static LivenessEvent success() {
        return new LivenessEvent(ProbeStatus.SUCCESS);
    }

    public static LivenessEvent failure() {
        return new LivenessEvent(ProbeStatus.FAILURE);
    }

    public static LivenessEvent unknown() {
        return new LivenessEvent(ProbeStatus.UNKNOWN);
    }
}