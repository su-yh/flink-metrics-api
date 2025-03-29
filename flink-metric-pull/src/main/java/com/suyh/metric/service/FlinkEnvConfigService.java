package com.suyh.metric.service;

import com.suyh.metric.mp.entity.mysql.FlinkEnvConfigEntity;
import com.suyh.metric.mp.mapper.mysql.FlinkEnvConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlinkEnvConfigService {
    private final FlinkEnvConfigMapper flinkEnvConfigMapper;

    public List<FlinkEnvConfigEntity> listAll() {
        return flinkEnvConfigMapper.selectList();
    }
}
