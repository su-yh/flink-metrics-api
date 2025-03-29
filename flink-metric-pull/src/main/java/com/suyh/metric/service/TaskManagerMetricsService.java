package com.suyh.metric.service;

import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mapper.mysql.TaskManagerMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author suyh
 * @since 2025-03-29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagerMetricsService {
    private final TaskManagerMetricsMapper taskManagerMetricsMapper;

    public List<TaskManagerMetricsEntity> listAll(String taskManagerId) {
        return taskManagerMetricsMapper.listAll(taskManagerId);
    }
}
