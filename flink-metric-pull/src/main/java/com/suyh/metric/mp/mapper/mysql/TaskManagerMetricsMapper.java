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
    default List<TaskManagerMetricsEntity> listQuery(String flinkEnvName, Integer maxNumber, Long startTs, Long endTs) {
        LambdaQueryWrapperX<TaskManagerMetricsEntity> queryWrapperX = build();

        queryWrapperX.eqIfPresent(TaskManagerMetricsEntity::getFlinkEnvName, flinkEnvName);
        queryWrapperX.geIfPresent(TaskManagerMetricsEntity::getTs, startTs);
        queryWrapperX.ltIfPresent(TaskManagerMetricsEntity::getTs, endTs);

        queryWrapperX.orderByAsc(TaskManagerMetricsEntity::getFlinkEnvName)
                .orderByAsc(TaskManagerMetricsEntity::getTs);

        // 最多查询3600 条
        queryWrapperX.last("limit " + maxNumber);

        return selectList(queryWrapperX);
    }
}
