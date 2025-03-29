package com.suyh.metric.mp.mapper.mysql;

import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mybatis.BaseMapperX;
import com.suyh.metric.mp.mybatis.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author suyh
 * @since 2025-03-29
 */
@Mapper
public interface TaskManagerMetricsMapper extends BaseMapperX<TaskManagerMetricsEntity> {
    default List<TaskManagerMetricsEntity> listAll(String taskManagerId) {
        LambdaQueryWrapperX<TaskManagerMetricsEntity> queryWrapperX = build();

        queryWrapperX.orderByAsc(TaskManagerMetricsEntity::getTaskManagerId)
                .orderByAsc(TaskManagerMetricsEntity::getTs);

        return selectList(queryWrapperX);
    }
}
