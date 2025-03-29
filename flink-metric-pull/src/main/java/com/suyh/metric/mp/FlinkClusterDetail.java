package com.suyh.metric.mp;

import com.suyh.metric.mp.entity.mysql.FlinkEnvConfigEntity;
import lombok.Data;

@Data
public class FlinkClusterDetail {
    private FlinkEnvConfigEntity flinkEnvConfigEntity;
    private String taskManagerId;
}
