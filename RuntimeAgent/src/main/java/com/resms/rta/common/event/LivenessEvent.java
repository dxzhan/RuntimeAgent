package com.resms.rta.common.event;

import com.resms.rta.common.meta.ProbeStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 活跃度事件
 *
 * @author sam
 */
public class LivenessEvent extends AbstractRTAEvent<Map> {
    private static final long serialVersionUID = 1L;
    private final String pid;
    private final String status;

    public LivenessEvent() {
        super(new HashMap());
        pid = null;
        status = ProbeStatus.UNKNOWN.name();
    }

    public LivenessEvent(String pid,ProbeStatus status) {
        super(new HashMap());
        this.pid = pid;
        this.status = status.name();
    }

    public String getPid() {
        return pid;
    }

    public String getStatus() {
        return status;
    }

    public static LivenessEvent success(String pid) {
        return new LivenessEvent(pid,ProbeStatus.SUCCESS);
    }

    public static LivenessEvent failure(String pid) {
        return new LivenessEvent(pid,ProbeStatus.FAILURE);
    }

    public static LivenessEvent unknown(String pid) {
        return new LivenessEvent(pid,ProbeStatus.UNKNOWN);
    }
}