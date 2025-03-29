package com.suyh.metric.mp.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.suyh.metric.constant.enums.YesOrNoEnums;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "tm_metric", autoResultMap = true)
public class FlinkEnvConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String flinkEnvName;
    private String flinkWebHost;
    private Integer flinkWebPort;

    private YesOrNoEnums enable;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date created;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updated;
}
