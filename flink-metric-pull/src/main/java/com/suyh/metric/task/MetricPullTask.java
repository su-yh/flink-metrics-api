package com.suyh.metric.task;

import com.suyh.metric.constant.Constants;
import com.suyh.metric.dto.rsp.TaskManagerDetailsRspDto;
import com.suyh.metric.dto.rsp.TaskManagerInfoDetail;
import com.suyh.metric.dto.rsp.TaskManagerMetricsByIdRspDto;
import com.suyh.metric.dto.rsp.TaskManagersInfoRspDto;
import com.suyh.metric.mp.entity.mysql.TaskManagerMetricsEntity;
import com.suyh.metric.mp.mapper.mysql.TaskManagerMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricPullTask {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private List<TaskManagerInfoDetail> managers;

    private final TaskManagerMetricsMapper taskManagerMetricsMapper;

    @PostConstruct
    public void init() throws Exception {
        managers = queryManager();

        scheduledExecutorService.scheduleWithFixedDelay(this::task, 10, 1, TimeUnit.SECONDS);
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
            executorService.submit(() -> queryTaskManagerMetricPlus(manager));
        }
    }

//    private final List<String> metricsParamsList = Arrays.asList("Status.JVM.Memory.Heap.Used",
//            "Status.JVM.Memory.Heap.Max",
//            "Status.Shuffle.Netty.UsedMemory",
//            "Status.Shuffle.Netty.TotalMemory",
//            "Status.Flink.Memory.Managed.Used",
//            "Status.Flink.Memory.Managed.Total",
//            "Status.JVM.Memory.Metaspace.Used",
//            "Status.JVM.Memory.Metaspace.Max");
//
//    private static final String metricsParams = "Status.JVM.Memory.Heap.Used," +
//            "Status.JVM.Memory.Heap.Max," +
//            "Status.Shuffle.Netty.UsedMemory," +
//            "Status.Shuffle.Netty.TotalMemory," +
//            "Status.Flink.Memory.Managed.Used," +
//            "Status.Flink.Memory.Managed.Total," +
//            "Status.JVM.Memory.Metaspace.Used," +
//            "Status.JVM.Memory.Metaspace.Max";

    private void queryTaskManagerMetricPlus(TaskManagerInfoDetail manager) {
        // 这里get 后面的值应该是可以通过api: http://192.168.8.143:8991/taskmanagers/localhost:34339-19078e/metrics 得到。
        // http://192.168.8.143:8991/taskmanagers/localhost:34339-19078e/metrics?get=Status.JVM.Memory.Heap.Used,Status.JVM.Memory.Heap.Max,Status.Shuffle.Netty.UsedMemory,Status.Shuffle.Netty.TotalMemory,Status.Flink.Memory.Managed.Used,Status.Flink.Memory.Managed.Total,Status.JVM.Memory.Metaspace.Used,Status.JVM.Memory.Metaspace.Max
        try {
            String metricsParams = String.join(",", Constants.STATUS_ID_LIST);
            String url = "http://192.168.8.143:8991/taskmanagers/{taskManagerId}/metrics";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            builder.queryParam("get", metricsParams);

            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("taskManagerId", manager.getId());
            URI uri = builder.buildAndExpand(pathParams).toUri();

            ResponseEntity<TaskManagerMetricsByIdRspDto[]> rsp = restTemplate.exchange(uri, HttpMethod.GET, null, TaskManagerMetricsByIdRspDto[].class);
            TaskManagerMetricsByIdRspDto[] rspDtos = rsp.getBody();
            System.out.println("body: " + rspDtos);
        } catch (Exception e) {
            log.error("queryTaskManagerMetric failed, managerId: {}", manager.getId(), e);
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
//            if (detailsRspDto.getNumberSlots().equals(detailsRspDto.getNumberAvailableSlots())) {
//                return;
//            }

            TaskManagerMetricsEntity entity = mappingEntity(detailsRspDto);
            entity.setTaskManagerId(manager.getId());
            entity.setTs(System.currentTimeMillis());   // 这里使用当前系统时间，而不使用 返回的心跳时间，没搞清楚那个时间戳为什么长时间都没有发生变化。
            taskManagerMetricsMapper.insert(entity);
        } catch (Exception e) {
            log.error("queryTaskManagerMetric failed, managerId: {}", manager.getId(), e);
        }
    }

    private TaskManagerMetricsEntity mappingEntity(TaskManagerDetailsRspDto detailsRspDto) {
        TaskManagerMetricsEntity entity = new TaskManagerMetricsEntity();
        BeanUtils.copyProperties(detailsRspDto.getMetrics(), entity);
        return entity;
    }
}
