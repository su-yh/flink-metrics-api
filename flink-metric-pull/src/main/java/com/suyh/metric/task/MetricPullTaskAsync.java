package com.suyh.metric.task;

import com.suyh.metric.constant.Constants;
import com.suyh.metric.dto.rsp.TaskManagerInfoDetail;
import com.suyh.metric.dto.rsp.TaskManagerMetricsByIdRspDto;
import com.suyh.metric.dto.rsp.TaskManagersInfoRspDto;
import com.suyh.metric.event.QueryTaskManagerMetricsSuccessEvent;
import com.suyh.metric.event.QueryTaskManagersSuccessEvent;
import com.suyh.metric.mp.FlinkClusterDetail;
import com.suyh.metric.mp.entity.mysql.FlinkEnvConfigEntity;
import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mapper.mysql.TaskManagerMetricsMapper;
import com.suyh.metric.service.FlinkEnvConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 使用异步请求
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricPullTaskAsync {
    private final WebClient webClient = WebClient.create();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    // key: env
    private final Map<String, FlinkClusterDetail> mapFlinkClusterDetail = new HashMap<>();

    private final ApplicationContext context;
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
        List<Mono<String>> requests = new ArrayList<>();

        mapFlinkClusterDetail.forEach((env, detail) -> {
            String taskManagerId = detail.getTaskManagerId();
            if (!StringUtils.hasText(taskManagerId)) {
                Mono<String> monoResult = queryTaskManagersMono(detail);
                requests.add(monoResult);
            } else {
                Mono<String> monoResult = queryTaskManagerMetricsMono(detail);
                requests.add(monoResult);
            }
        });

        Flux<String> dynamicFlux = Flux.merge(requests);
        dynamicFlux.blockLast(); // 阻塞等待全部请求都结束
    }

    private Mono<String> queryTaskManagersMono(FlinkClusterDetail detail) {
        FlinkEnvConfigEntity flinkEnvConfigEntity = detail.getFlinkEnvConfigEntity();
        String env = flinkEnvConfigEntity.getFlinkEnvName();

        // http://%s:%d/taskmanagers
        Function<UriBuilder, URI> uriFunction = uriBuilder -> uriBuilder
                .scheme("http")
                .host(flinkEnvConfigEntity.getFlinkWebHost())
                .port(flinkEnvConfigEntity.getFlinkWebPort())
                .path("/taskmanagers")
                .build();

        Mono<TaskManagersInfoRspDto> responseMono = webClient.get().uri(uriFunction).retrieve().bodyToMono(TaskManagersInfoRspDto.class);
        return responseMono.doOnSuccess(rspDto -> context.publishEvent(new QueryTaskManagersSuccessEvent(env, rspDto)))
                .doOnError(error -> System.out.println("error: " + error))
                .map(rspDto -> "OK");
    }

    private Mono<String> queryTaskManagerMetricsMono(FlinkClusterDetail detail) {
        String taskManagerId = detail.getTaskManagerId();
        FlinkEnvConfigEntity flinkEnvConfigEntity = detail.getFlinkEnvConfigEntity();
        String env = flinkEnvConfigEntity.getFlinkEnvName();

        String metricsParams = String.join(",", Constants.STATUS_ID_LIST);
        // http://%s:%d/taskmanagers/{taskManagerId}/metrics?get=xxx,xxx,xxx,xxx
        Function<UriBuilder, URI> uriFunction = uriBuilder -> uriBuilder
                .scheme("http")
                .host(flinkEnvConfigEntity.getFlinkWebHost())
                .port(flinkEnvConfigEntity.getFlinkWebPort())
                .path("/taskmanagers/{taskManagerId}/metrics")
                .queryParam("get", metricsParams)
                .build(taskManagerId);

        Mono<TaskManagerMetricsByIdRspDto[]> responseMono = webClient.get().uri(uriFunction).retrieve()
                .bodyToMono(TaskManagerMetricsByIdRspDto[].class);

        return responseMono.doOnSuccess(rspDtos -> context.publishEvent(new QueryTaskManagerMetricsSuccessEvent(env, rspDtos)))
                .doOnError(error -> detail.setTaskManagerId(null))
                .map(rspDtos -> "OK");
    }

    @EventListener(QueryTaskManagersSuccessEvent.class)
    public void postTaskManager(QueryTaskManagersSuccessEvent event) {
        FlinkClusterDetail flinkClusterDetail = mapFlinkClusterDetail.get(event.env);

        if (flinkClusterDetail == null) {
            return;
        }
        if (event.rspDto == null) {
            return;
        }

        List<TaskManagerInfoDetail> managers = event.rspDto.getManagers();
        if (managers == null || managers.isEmpty()) {
            return;
        }

        TaskManagerInfoDetail taskManagerInfoDetail = managers.get(0);
        if (taskManagerInfoDetail == null) {
            return;
        }

        flinkClusterDetail.setTaskManagerId(taskManagerInfoDetail.getId());
        log.info("task manager id: {}", taskManagerInfoDetail.getId());
    }

    @EventListener(QueryTaskManagerMetricsSuccessEvent.class)
    public void postTaskManagerMetrics(QueryTaskManagerMetricsSuccessEvent event) {
        FlinkClusterDetail flinkClusterDetail = mapFlinkClusterDetail.get(event.env);
        if (flinkClusterDetail == null) {
            return;
        }

        try {
            FlinkEnvConfigEntity flinkEnvConfigEntity = flinkClusterDetail.getFlinkEnvConfigEntity();
            TaskManagerMetricsEntity entity = mappingEntity(event.rspDtos);
            entity.setFlinkEnvName(flinkEnvConfigEntity.getFlinkEnvName());
            entity.setTs(System.currentTimeMillis());   // 这里使用当前系统时间，而不使用 返回的心跳时间，没搞清楚那个时间戳为什么长时间都没有发生变化。
            taskManagerMetricsMapper.insert(entity);
            log.info("task manager metrics finished, env: {}", event.env);
        } catch (Exception e) {
            // 失败，则重置taskManagerId，使得重新
            flinkClusterDetail.setTaskManagerId(null);
        }
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
