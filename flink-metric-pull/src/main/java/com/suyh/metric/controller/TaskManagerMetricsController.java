package com.suyh.metric.controller;

import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.service.TaskManagerMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author suyh
 * @since 2025-03-29
 */
@Tag(name = "TaskManager指标")
@RestController
@RequestMapping("/task/manager")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TaskManagerMetricsController {
    private final TaskManagerMetricsService taskManagerMetricsService;

    @CrossOrigin
    @Operation(summary = "【TaskManager指标】查询-全量")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public List<TaskManagerMetricsEntity> listAll(
            @RequestParam("flinkWebHost") String flinkWebHost,
            @RequestParam("flinkWebPort") Integer flinkWebPort) {
        return taskManagerMetricsService.listAll(flinkWebHost, flinkWebPort);
    }
}
