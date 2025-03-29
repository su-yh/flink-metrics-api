package com.suyh.metric.dto.rsp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagerMetricsInfo;

/**
 * @author suyh
 * @see TaskManagerMetricsInfo
 * @since 2025-03-28
 */
@Data
public class TaskManagerMetrics {

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_HEAP_USED)
    private Long heapUsed;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_HEAP_COMMITTED)
    private Long heapCommitted;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_HEAP_MAX)
    private Long heapMax;

    // --------- Non heap memory -------------

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_NON_HEAP_USED)
    private Long nonHeapUsed;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_NON_HEAP_COMMITTED)
    private Long nonHeapCommitted;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_NON_HEAP_MAX)
    private Long nonHeapMax;

    // --------- Direct buffer pool -------------

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_DIRECT_COUNT)
    private Long directCount;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_DIRECT_USED)
    private Long directUsed;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_DIRECT_MAX)
    private Long directMax;

    // --------- Mapped buffer pool -------------

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_MAPPED_COUNT)
    private Long mappedCount;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_MAPPED_USED)
    private Long mappedUsed;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_MAPPED_MAX)
    private Long mappedMax;

    // --------- Shuffle Netty buffer pool -------------

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_SHUFFLE_MEMORY_SEGMENTS_AVAILABLE)
    private Long shuffleMemorySegmentsAvailable;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_SHUFFLE_MEMORY_SEGMENTS_USED)
    private Long shuffleMemorySegmentsUsed;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_SHUFFLE_MEMORY_SEGMENTS_TOTAL)
    private Long shuffleMemorySegmentsTotal;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_SHUFFLE_MEMORY_AVAILABLE)
    private Long shuffleMemoryAvailable;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_SHUFFLE_MEMORY_USED)
    private Long shuffleMemoryUsed;

    @JsonProperty(TaskManagerMetricsInfo.FIELD_NAME_SHUFFLE_MEMORY_TOTAL)
    private Long shuffleMemoryTotal;
}
