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
    default List<TaskManagerMetricsEntity> listAll(String flinkWebHost, Integer flinkWebPort) {
        LambdaQueryWrapperX<TaskManagerMetricsEntity> queryWrapperX = build();

        queryWrapperX.eqIfPresent(TaskManagerMetricsEntity::getFlinkWebHost, flinkWebHost);
        queryWrapperX.eqIfPresent(TaskManagerMetricsEntity::getFlinkWebPort, flinkWebPort);

        queryWrapperX.orderByAsc(TaskManagerMetricsEntity::getFlinkWebHost)
                .orderByAsc(TaskManagerMetricsEntity::getFlinkWebPort)
                .orderByAsc(TaskManagerMetricsEntity::getTs);

        return selectList(queryWrapperX);
    }
}
