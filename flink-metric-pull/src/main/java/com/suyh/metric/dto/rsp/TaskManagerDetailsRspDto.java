package com.suyh.metric.dto.rsp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagerDetailsInfo;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagerInfo;

/**
 * @author suyh
 * @since 2025-03-28
 * @see TaskManagerDetailsInfo
 */
@Data
public class TaskManagerDetailsRspDto {
    @JsonProperty(TaskManagerDetailsInfo.FIELD_NAME_METRICS)
    private TaskManagerMetrics metrics;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_LAST_HEARTBEAT)
    private Long timeSinceLastHeartbeat;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_NUMBER_SLOTS)
    private Integer numberSlots;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_NUMBER_AVAILABLE_SLOTS)
    private Integer numberAvailableSlots;
}
