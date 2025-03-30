package com.suyh.metric.event;

import com.suyh.metric.dto.rsp.TaskManagerMetricsByIdRspDto;
import org.springframework.context.ApplicationEvent;

public class QueryTaskManagerMetricsSuccessEvent extends ApplicationEvent {
    public final String env;
    public final TaskManagerMetricsByIdRspDto[] rspDtos;

    public QueryTaskManagerMetricsSuccessEvent(String env, TaskManagerMetricsByIdRspDto[] rspDtos) {
        super(env);
        this.env = env;
        this.rspDtos = rspDtos;
    }
}
