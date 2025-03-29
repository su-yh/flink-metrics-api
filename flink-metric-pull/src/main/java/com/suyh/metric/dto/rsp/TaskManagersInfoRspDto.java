package com.suyh.metric.dto.rsp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagersInfo;

import java.util.List;

/**
 * @author suyh
 * @since 2025-03-28
 * @see TaskManagersInfo
 */
@Data
public class TaskManagersInfoRspDto {
    @JsonProperty(TaskManagersInfo.FIELD_NAME_TASK_MANAGERS)
    private List<TaskManagerInfoDetail> managers;
}
