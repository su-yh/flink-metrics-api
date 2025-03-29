package com.suyh.metric.dto.rsp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.instance.HardwareDescription;
import org.apache.flink.runtime.rest.messages.ResourceProfileInfo;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagerInfo;
import org.apache.flink.runtime.taskexecutor.TaskExecutorMemoryConfiguration;

/**
 * @author suyh
 * @see TaskManagerInfo
 * @since 2025-03-28
 */
@Data
public class TaskManagerInfoDetail {
    /**
     * 这是正常的flink 的属性，但是懒得处理序列化的问题，所以直接使用String了
     */
    @JsonIgnore
    @Deprecated
    private ResourceID resourceId;

    /**
     * @see #resourceId
     */
    @JsonProperty(TaskManagerInfo.FIELD_NAME_RESOURCE_ID)
    private String id;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_ADDRESS)
    private String address;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_DATA_PORT)
    private Integer dataPort;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_JMX_PORT)
    private Integer jmxPort;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_LAST_HEARTBEAT)
    private Long lastHeartbeat;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_NUMBER_SLOTS)
    private Integer numberSlots;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_NUMBER_AVAILABLE_SLOTS)
    private Integer numberAvailableSlots;

    @JsonIgnore
    @JsonProperty(TaskManagerInfo.FIELD_NAME_TOTAL_RESOURCE)
    private ResourceProfileInfo totalResource;

    @JsonIgnore
    @JsonProperty(TaskManagerInfo.FIELD_NAME_AVAILABLE_RESOURCE)
    private ResourceProfileInfo freeResource;

    @JsonIgnore
    @JsonProperty(TaskManagerInfo.FIELD_NAME_HARDWARE)
    private HardwareDescription hardwareDescription;

    @JsonIgnore
    @JsonProperty(TaskManagerInfo.FIELD_NAME_MEMORY)
    private TaskExecutorMemoryConfiguration memoryConfiguration;

    @JsonProperty(TaskManagerInfo.FIELD_NAME_BLOCKED)
    private Boolean blocked;

}
