package com.suyh.metric.controller;

import com.suyh.metric.mp.entity.mysql.FlinkEnvConfigEntity;
import com.suyh.metric.service.FlinkEnvConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "flink 集群环境配置")
@RestController
@RequestMapping("/flink/cluster/env")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FlinkEnvConfigController {
    private final FlinkEnvConfigService flinkEnvConfigService;

    @CrossOrigin
    @Operation(summary = "【flink 集群环境配置】查询-全量")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public List<FlinkEnvConfigEntity> listAll() {
        return flinkEnvConfigService.listAll();
    }
}
