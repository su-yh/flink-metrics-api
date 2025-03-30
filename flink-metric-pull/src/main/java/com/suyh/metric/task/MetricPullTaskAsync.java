package com.suyh.metric.task;

import com.suyh.metric.constant.Constants;
import com.suyh.metric.dto.rsp.TaskManagerMetricsByIdRspDto;
import com.suyh.metric.dto.rsp.TaskManagersInfoRspDto;
import com.suyh.metric.mp.FlinkClusterDetail;
import com.suyh.metric.mp.entity.mysql.FlinkEnvConfigEntity;
import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mapper.mysql.TaskManagerMetricsMapper;
import com.suyh.metric.service.FlinkEnvConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
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

    public static enum FlinkApi {
        QUERY_TASK_MANAGERS("queryTaskManager"),
        QUERY_MANAGER_METRICS("queryManagerMetrics"),
        ;

        public final String code;

        FlinkApi(String code) {
            this.code = code;
        }
    }

    @Data
    public static class TmpVo {
        private String env;
        private FlinkApi flinkApi;
        private Class<?> rspClazz;
        private Object rspDto;
    }

    public static class QueryTaskManagersSuccessEvent extends ApplicationEvent {
        public final String env;
        public final TaskManagersInfoRspDto rspDto;

        public QueryTaskManagersSuccessEvent(String env, TaskManagersInfoRspDto rspDto) {
            super(env);
            this.env = env;
            this.rspDto = rspDto;
        }
    }

    public static class QueryTaskManagerMetricsSuccessEvent extends ApplicationEvent {
        public final String env;
        public final TaskManagerMetricsByIdRspDto[] rspDtos;

        public QueryTaskManagerMetricsSuccessEvent(String env, TaskManagerMetricsByIdRspDto[] rspDtos) {
            super(env);
            this.env = env;
            this.rspDtos = rspDtos;
        }
    }

    public void task() {
        List<Mono<String>> requests = new ArrayList<>();

        mapFlinkClusterDetail.forEach((env, detail) -> {
            FlinkEnvConfigEntity flinkEnvConfigEntity = detail.getFlinkEnvConfigEntity();
            String taskManagerId = detail.getTaskManagerId();
            if (!StringUtils.hasText(taskManagerId)) {
                // http://%s:%d/taskmanagers
                Function<UriBuilder, URI> uriFunction = uriBuilder -> uriBuilder
                        .scheme("http")
                        .host(flinkEnvConfigEntity.getFlinkWebHost())
                        .port(flinkEnvConfigEntity.getFlinkWebPort())
                        .path("/taskmanagers")
                        .build();

                Mono<TaskManagersInfoRspDto> responseMono = webClient.get().uri(uriFunction).retrieve().bodyToMono(TaskManagersInfoRspDto.class);
                Mono<String> monoResult = responseMono.doOnSuccess(rspDto -> context.publishEvent(new QueryTaskManagersSuccessEvent(env, rspDto)))
                        .doOnError(error -> System.out.println("error: " + error))
                        .map(rspDto -> "OK");
//                Mono<ApplicationEvent> monoResult = responseMono.map(rspDto -> {
//                    return new QueryTaskManagersSuccessEvent(rspDto);
//                    return () -> {
//                        if (rspDto == null) {
//                            return;
//                        }
//                        List<TaskManagerInfoDetail> managers = rspDto.getManagers();
//                        if (managers == null || managers.isEmpty()) {
//                            return;
//                        }
//
//                        TaskManagerInfoDetail taskManagerInfoDetail = managers.get(0);
//                        if (taskManagerInfoDetail == null) {
//                            return;
//                        }
//
//                        detail.setTaskManagerId(taskManagerInfoDetail.getId());
//                    };
//                });
                requests.add(monoResult);
            } else {
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

                Mono<String> monoResult = responseMono.doOnSuccess(rspDtos -> context.publishEvent(new QueryTaskManagerMetricsSuccessEvent(env, rspDtos)))
                        .doOnError(error -> detail.setTaskManagerId(null))
                        .map(rspDtos -> "OK");
                requests.add(monoResult);
            }
        });

        Flux<String> dynamicFlux = Flux.merge(requests);
//        dynamicFlux.subscribe(context::publishEvent, error -> System.out.println("Error: " + error));

        dynamicFlux.blockLast();
        System.out.println("blockLast finished.");
    }

    @EventListener(QueryTaskManagersSuccessEvent.class)
    public void postTaskManager(QueryTaskManagersSuccessEvent event) {
        log.info("evn: {}, managerId: {}", event.env, event.rspDto.getManagers().get(0).getId());
        FlinkClusterDetail flinkClusterDetail = mapFlinkClusterDetail.get(event.env);
        flinkClusterDetail.setTaskManagerId(event.rspDto.getManagers().get(0).getId());
        // TODO: suyh - 处理逻辑
    }

    @EventListener(QueryTaskManagerMetricsSuccessEvent.class)
    public void postTaskManagerMetrics(QueryTaskManagerMetricsSuccessEvent event) {
        for (TaskManagerMetricsByIdRspDto rspDto : event.rspDtos) {
            log.info("env: {}, id: {}, value: {}", event.env, rspDto.getId(), rspDto.getValue());
        }


        FlinkClusterDetail flinkClusterDetail = mapFlinkClusterDetail.get(event.env);
        // TODO: suyh - 处理逻辑，插入数据库

    }

    private void postTaskManagerMetrics(FlinkClusterDetail detail, TaskManagerMetricsByIdRspDto[] rspDtos) {
        try {
            FlinkEnvConfigEntity flinkEnvConfigEntity = detail.getFlinkEnvConfigEntity();
            TaskManagerMetricsEntity entity = mappingEntity(rspDtos);
            entity.setFlinkEnvName(flinkEnvConfigEntity.getFlinkEnvName());
            entity.setTs(System.currentTimeMillis());   // 这里使用当前系统时间，而不使用 返回的心跳时间，没搞清楚那个时间戳为什么长时间都没有发生变化。
            taskManagerMetricsMapper.insert(entity);
        } catch (Exception e) {
            // 失败，则重置taskManagerId，使得重新
            detail.setTaskManagerId(null);
        }
    }

//    private void queryTaskManagerMetricPlus(FlinkClusterDetail flinkClusterDetail) {
//        // 这里get 后面的值应该是可以通过api: http://192.168.8.143:8991/taskmanagers/localhost:34339-19078e/metrics 得到。
//        // http://192.168.8.143:8991/taskmanagers/localhost:34339-19078e/metrics?get=Status.JVM.Memory.Heap.Used,Status.JVM.Memory.Heap.Max,Status.Shuffle.Netty.UsedMemory,Status.Shuffle.Netty.TotalMemory,Status.Flink.Memory.Managed.Used,Status.Flink.Memory.Managed.Total,Status.JVM.Memory.Metaspace.Used,Status.JVM.Memory.Metaspace.Max
//        FlinkEnvConfigEntity flinkEnvConfigEntity = flinkClusterDetail.getFlinkEnvConfigEntity();
//        String taskManagerId = flinkClusterDetail.getTaskManagerId();
//        if (!StringUtils.hasText(taskManagerId)) {
//            taskManagerId = queryTaskManagerId(flinkEnvConfigEntity);
//            if (!StringUtils.hasText(taskManagerId)) {
//                log.warn("QUERY TASK MANAGER ID FAILED, FLINK CLUSTER ENV: {}", flinkEnvConfigEntity.getFlinkEnvName());
//                return;
//            }
//        }
//
//        try {
//            String metricsParams = String.join(",", Constants.STATUS_ID_LIST);
//            String url = String.format("http://%s:%d/taskmanagers/{taskManagerId}/metrics",
//                    flinkEnvConfigEntity.getFlinkWebHost(), flinkEnvConfigEntity.getFlinkWebPort());
//            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
//            builder.queryParam("get", metricsParams);
//
//            Map<String, String> pathParams = new HashMap<>();
//            pathParams.put("taskManagerId", taskManagerId);
//            URI uri = builder.buildAndExpand(pathParams).toUri();
//
//            ResponseEntity<TaskManagerMetricsByIdRspDto[]> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagerMetricsByIdRspDto[].class);
//            TaskManagerMetricsByIdRspDto[] rspDtos = rsp.getBody();
//            assert rspDtos != null;
//            TaskManagerMetricsEntity entity = mappingEntity(rspDtos);
//            entity.setFlinkEnvName(flinkEnvConfigEntity.getFlinkEnvName());
//            entity.setTs(System.currentTimeMillis());   // 这里使用当前系统时间，而不使用 返回的心跳时间，没搞清楚那个时间戳为什么长时间都没有发生变化。
//            taskManagerMetricsMapper.insert(entity);
//        } catch (Exception e) {
//            log.error("queryTaskManagerMetric failed, taskManagerId: {}", taskManagerId, e);
//            // 发起taskManager 请求出现了异常，则认为这个taskManagerId 失效了，需要重新拉取最新的
//            flinkClusterDetail.setTaskManagerId(null);
//        }
//    }
//
//    private String queryTaskManagerId(FlinkEnvConfigEntity flinkEnvConfigEntity) {
//        String taskManagerId = null;
//        try {
//            String url = String.format("http://%s:%d/taskmanagers",
//                    flinkEnvConfigEntity.getFlinkWebHost(), flinkEnvConfigEntity.getFlinkWebPort());
//            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
//            URI uri = builder.build().toUri();
//            ResponseEntity<TaskManagersInfoRspDto> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagersInfoRspDto.class);
//            TaskManagersInfoRspDto body = rsp.getBody();
//            if (body == null) {
//                return null;
//            }
//
//            List<TaskManagerInfoDetail> managers = body.getManagers();
//            if (managers == null || managers.isEmpty()) {
//                return null;
//            }
//
//            TaskManagerInfoDetail taskManagerInfoDetail = managers.get(0);
//            if (taskManagerInfoDetail == null) {
//                return null;
//            }
//
//            taskManagerId = taskManagerInfoDetail.getId();
//        } catch (Exception e) {
//            log.error("queryTaskManagerId failed. env: {}", flinkEnvConfigEntity.getFlinkEnvName());
//        }
//
//        return taskManagerId;
//    }

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
