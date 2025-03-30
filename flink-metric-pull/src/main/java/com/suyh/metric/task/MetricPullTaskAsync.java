package com.suyh.metric.task;

import com.suyh.metric.constant.Constants;
import com.suyh.metric.dto.rsp.TaskManagerInfoDetail;
import com.suyh.metric.dto.rsp.TaskManagerMetricsByIdRspDto;
import com.suyh.metric.dto.rsp.TaskManagersInfoRspDto;
import com.suyh.metric.mp.FlinkClusterDetail;
import com.suyh.metric.mp.entity.mysql.FlinkEnvConfigEntity;
import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mapper.mysql.TaskManagerMetricsMapper;
import com.suyh.metric.service.FlinkEnvConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 使用异步请求
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricPullTaskAsync {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    // key: env
    private final Map<String, FlinkClusterDetail> mapFlinkClusterDetail = new HashMap<>();

    private final TaskManagerMetricsMapper taskManagerMetricsMapper;

    private final FlinkEnvConfigService flinkEnvConfigService;

    @PostConstruct
    public void init() throws Exception {
        List<FlinkEnvConfigEntity> flinkEnvConfigEntities = flinkEnvConfigService.listAll();
        if (flinkEnvConfigEntities == null || flinkEnvConfigEntities.isEmpty()) {
            log.warn("{} IS EMPTY.", FlinkEnvConfigEntity.class.getSimpleName());
            return;
        }

        for (FlinkEnvConfigEntity flinkEnvConfigEntity : flinkEnvConfigEntities) {
            FlinkClusterDetail detail = new FlinkClusterDetail();
            detail.setFlinkEnvConfigEntity(flinkEnvConfigEntity);
            mapFlinkClusterDetail.put(flinkEnvConfigEntity.getFlinkEnvName(), detail);
        }

        scheduledExecutorService.scheduleWithFixedDelay(this::task, 1, 1, TimeUnit.SECONDS);
    }

    public void task() {
        mapFlinkClusterDetail.forEach((env, flinkClusterDetail) -> queryTaskManagerMetricPlus(flinkClusterDetail));
    }

    private void queryTaskManagerMetricPlus(FlinkClusterDetail flinkClusterDetail) {
        // 这里get 后面的值应该是可以通过api: http://192.168.8.143:8991/taskmanagers/localhost:34339-19078e/metrics 得到。
        // http://192.168.8.143:8991/taskmanagers/localhost:34339-19078e/metrics?get=Status.JVM.Memory.Heap.Used,Status.JVM.Memory.Heap.Max,Status.Shuffle.Netty.UsedMemory,Status.Shuffle.Netty.TotalMemory,Status.Flink.Memory.Managed.Used,Status.Flink.Memory.Managed.Total,Status.JVM.Memory.Metaspace.Used,Status.JVM.Memory.Metaspace.Max
        FlinkEnvConfigEntity flinkEnvConfigEntity = flinkClusterDetail.getFlinkEnvConfigEntity();
        String taskManagerId = flinkClusterDetail.getTaskManagerId();
        if (!StringUtils.hasText(taskManagerId)) {
            taskManagerId = queryTaskManagerId(flinkEnvConfigEntity);
            if (!StringUtils.hasText(taskManagerId)) {
                log.warn("QUERY TASK MANAGER ID FAILED, FLINK CLUSTER ENV: {}", flinkEnvConfigEntity.getFlinkEnvName());
                return;
            }
        }

        try {
            String metricsParams = String.join(",", Constants.STATUS_ID_LIST);
            String url = String.format("http://%s:%d/taskmanagers/{taskManagerId}/metrics",
                    flinkEnvConfigEntity.getFlinkWebHost(), flinkEnvConfigEntity.getFlinkWebPort());
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            builder.queryParam("get", metricsParams);

            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("taskManagerId", taskManagerId);
            URI uri = builder.buildAndExpand(pathParams).toUri();

            ResponseEntity<TaskManagerMetricsByIdRspDto[]> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagerMetricsByIdRspDto[].class);
            TaskManagerMetricsByIdRspDto[] rspDtos = rsp.getBody();
            assert rspDtos != null;
            TaskManagerMetricsEntity entity = mappingEntity(rspDtos);
            entity.setFlinkEnvName(flinkEnvConfigEntity.getFlinkEnvName());
            entity.setTs(System.currentTimeMillis());   // 这里使用当前系统时间，而不使用 返回的心跳时间，没搞清楚那个时间戳为什么长时间都没有发生变化。
            taskManagerMetricsMapper.insert(entity);
        } catch (Exception e) {
            log.error("queryTaskManagerMetric failed, taskManagerId: {}", taskManagerId, e);
            // 发起taskManager 请求出现了异常，则认为这个taskManagerId 失效了，需要重新拉取最新的
            flinkClusterDetail.setTaskManagerId(null);
        }
    }

    private String queryTaskManagerId(FlinkEnvConfigEntity flinkEnvConfigEntity) {
        String taskManagerId = null;
        try {
            String url = "http://192.168.8.143:8991/taskmanagers";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            URI uri = builder.build().toUri();
            ResponseEntity<TaskManagersInfoRspDto> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagersInfoRspDto.class);
            TaskManagersInfoRspDto body = rsp.getBody();
            if (body == null) {
                return null;
            }

            List<TaskManagerInfoDetail> managers = body.getManagers();
            if (managers == null || managers.isEmpty()) {
                return null;
            }

            TaskManagerInfoDetail taskManagerInfoDetail = managers.get(0);
            if (taskManagerInfoDetail == null) {
                return null;
            }

            taskManagerId = taskManagerInfoDetail.getId();
        } catch (Exception e) {
            log.error("queryTaskManagerId failed. env: {}", flinkEnvConfigEntity.getFlinkEnvName());
        }

        return taskManagerId;
    }

    private TaskManagerMetricsEntity mappingEntity(TaskManagerMetricsByIdRspDto[] rspDtos) {
        TaskManagerMetricsEntity entity = new TaskManagerMetricsEntity();

        for (TaskManagerMetricsByIdRspDto rspDto : rspDtos) {
            switch (rspDto.getId()) {
                case Constants.STATUS_FLINK_MEMORY_MANAGED_USED:
                    entity.setFlinkMemoryManagerUsed(rspDto.getValue());
                    break;
                case Constants.STATUS_FLINK_MEMORY_MANAGED_TOTAL:
                    entity.setFlinkMemoryManagerTotal(rspDto.getValue());
                    break;
                case Constants.STATUS_JVM_MEMORY_METASPACE_USED:
                    entity.setJvmMemoryMetaspaceUsed(rspDto.getValue());
                    break;
                case Constants.STATUS_JVM_MEMORY_METASPACE_MAX:
                    entity.setJvmMemoryMetaspaceMax(rspDto.getValue());
                    break;
                case Constants.STATUS_JVM_MEMORY_HEAP_USED:
                    entity.setHeapUsed(rspDto.getValue());
                    break;
                case Constants.STATUS_JVM_MEMORY_HEAP_MAX:
                    entity.setHeapMax(rspDto.getValue());
                    break;
                case Constants.STATUS_SHUFFLE_NETTY_USED_MEMORY:
                    entity.setShuffleMemoryUsed(rspDto.getValue());
                    break;
                case Constants.STATUS_SHUFFLE_NETTY_TOTAL_MEMORY:
                    entity.setShuffleMemoryTotal(rspDto.getValue());
                    break;
                default:
                    break;
            }
        }

        return entity;
    }
}
