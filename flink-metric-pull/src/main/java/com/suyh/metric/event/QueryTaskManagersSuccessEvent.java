package com.suyh.metric.event;

import com.suyh.metric.dto.rsp.TaskManagersInfoRspDto;
import org.springframework.context.ApplicationEvent;

public class QueryTaskManagersSuccessEvent extends ApplicationEvent {
    public final String env;
    public final TaskManagersInfoRspDto rspDto;

    public QueryTaskManagersSuccessEvent(String env, TaskManagersInfoRspDto rspDto) {
        super(env);
        this.env = env;
        this.rspDto = rspDto;
    }
}