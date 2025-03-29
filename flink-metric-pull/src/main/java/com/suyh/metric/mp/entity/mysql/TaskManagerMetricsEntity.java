package com.suyh.metric.mp.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.suyh.metric.dto.rsp.TaskManagerMetrics;
import lombok.Data;

import java.util.Date;

/**
 * @author suyh
 * @since 2025-03-29
 */
@Data
@TableName(value = "tm_metric", autoResultMap = true)
public class TaskManagerMetricsEntity extends TaskManagerMetrics {
    @TableId(type = IdType.AUTO)
    private Long id;

    // private String taskManagerId;
    private String flinkWebHost;
    private Integer flinkWebPort;
    private Long ts;
    private Long flinkMemoryManagerUsed;
    private Long flinkMemoryManagerTotal;
    private Long jvmMemoryMetaspaceUsed;
    private Long jvmMemoryMetaspaceMax;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date created;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updated;
}
