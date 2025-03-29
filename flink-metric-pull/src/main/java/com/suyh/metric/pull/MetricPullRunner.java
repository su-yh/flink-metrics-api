package com.suyh.metric.pull;

import com.suyh.metric.dto.rsp.TaskManagerDetailsRspDto;
import com.suyh.metric.dto.rsp.TaskManagerInfoDetail;
import com.suyh.metric.dto.rsp.TaskManagersInfoRspDto;
import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mapper.mysql.TaskManagerMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author suyh
 * @since 2025-03-28
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricPullRunner implements ApplicationRunner {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private List<TaskManagerInfoDetail> managers;

    private final TaskManagerMetricsMapper taskManagerMetricsMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        managers = queryManager();

        scheduledExecutorService.scheduleWithFixedDelay(this::task, 1, 1, TimeUnit.SECONDS);
    }

    private List<TaskManagerInfoDetail> queryManager() {
        String url = "http://192.168.8.143:8991/taskmanagers";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        URI uri = builder.build().toUri();
        ResponseEntity<TaskManagersInfoRspDto> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagersInfoRspDto.class);
        TaskManagersInfoRspDto body = rsp.getBody();
        return body.getManagers();
    }

    public void task() {
        if (managers == null || managers.isEmpty()) {
            log.warn("managers is empty.");
            return;
        }

        for (TaskManagerInfoDetail manager : managers) {
            executorService.submit(() -> queryTaskManagerMetric(manager));
        }
    }

    public void queryTaskManagerMetric(TaskManagerInfoDetail manager) {
        try {
            String url = "http://192.168.8.143:8991/taskmanagers/{taskManagerId}";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("taskManagerId", manager.getId());
            URI uri = builder.buildAndExpand(pathParams).toUri();

            ResponseEntity<TaskManagerDetailsRspDto> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagerDetailsRspDto.class);
            TaskManagerDetailsRspDto detailsRspDto = rsp.getBody();
            assert detailsRspDto != null;
            if (detailsRspDto.getNumberSlots().equals(detailsRspDto.getNumberAvailableSlots())) {
                return;
            }

            TaskManagerMetricsEntity entity = mappingEntity(detailsRspDto);
            entity.setTaskManagerId(manager.getId());
            taskManagerMetricsMapper.insert(entity);
        } catch (Exception e) {
            log.error("queryTaskManagerMetric failed, managerId: {}", manager.getId(), e);
        }
    }

    private TaskManagerMetricsEntity mappingEntity(TaskManagerDetailsRspDto detailsRspDto) {
        TaskManagerMetricsEntity entity = new TaskManagerMetricsEntity();
        BeanUtils.copyProperties(detailsRspDto.getMetrics(), entity);
        entity.setTs(detailsRspDto.getTimeSinceLastHeartbeat());
        return entity;
    }
}
