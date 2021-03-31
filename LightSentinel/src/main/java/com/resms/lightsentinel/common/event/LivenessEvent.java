package com.resms.lightsentinel.common.event;

import com.resms.lightsentinel.common.meta.ProbeStatus;

/**
 * 活跃度事件
 *
 * @author sam
 */
public class LivenessEvent extends AbstractLightSentinelEvent<String> {
    private static final long serialVersionUID = 1L;
    private final String nodeId;
    private final String status;

    public LivenessEvent() {
        this(null,ProbeStatus.UNKNOWN);
    }

    public LivenessEvent(String nodeId, ProbeStatus status) {
        super("LivenessEvent");
        this.nodeId = nodeId;
        this.status = status.name();
    }

    public String getNodeId() {
        return nodeId;
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