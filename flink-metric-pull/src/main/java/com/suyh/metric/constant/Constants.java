package com.suyh.metric.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author suyh
 * @since 2025-03-29
 */
public class Constants {
    public static final String STATUS_FLINK_MEMORY_MANAGED_USED = "Status.Flink.Memory.Managed.Used";
    public static final String STATUS_FLINK_MEMORY_MANAGED_TOTAL = "Status.Flink.Memory.Managed.Total";
    public static final String STATUS_JVM_MEMORY_METASPACE_USED = "Status.JVM.Memory.Metaspace.Used";
    public static final String STATUS_JVM_MEMORY_METASPACE_MAX = "Status.JVM.Memory.Metaspace.Max";


    // JVM 堆内存指标常量
    public static final String STATUS_JVM_MEMORY_HEAP_USED = "Status.JVM.Memory.Heap.Used";
    public static final String STATUS_JVM_MEMORY_HEAP_COMMITTED = "Status.JVM.Memory.Heap.Committed";
    public static final String STATUS_JVM_MEMORY_HEAP_MAX = "Status.JVM.Memory.Heap.Max";

    // JVM 非堆内存指标常量
    public static final String STATUS_JVM_MEMORY_NON_HEAP_USED = "Status.JVM.Memory.NonHeap.Used";
    public static final String STATUS_JVM_MEMORY_NON_HEAP_COMMITTED = "Status.JVM.Memory.NonHeap.Committed";
    public static final String STATUS_JVM_MEMORY_NON_HEAP_MAX = "Status.JVM.Memory.NonHeap.Max";

    // 直接内存指标常量
    public static final String STATUS_JVM_MEMORY_DIRECT_COUNT = "Status.JVM.Memory.Direct.Count";
    public static final String STATUS_JVM_MEMORY_DIRECT_MEMORY_USED = "Status.JVM.Memory.Direct.MemoryUsed";
    public static final String STATUS_JVM_MEMORY_DIRECT_TOTAL_CAPACITY = "Status.JVM.Memory.Direct.TotalCapacity";

    // 映射内存指标常量
    public static final String STATUS_JVM_MEMORY_MAPPED_COUNT = "Status.JVM.Memory.Mapped.Count";
    public static final String STATUS_JVM_MEMORY_MAPPED_MEMORY_USED = "Status.JVM.Memory.Mapped.MemoryUsed";
    public static final String STATUS_JVM_MEMORY_MAPPED_TOTAL_CAPACITY = "Status.JVM.Memory.Mapped.TotalCapacity";

    // 网络内存段指标常量
    public static final String STATUS_SHUFFLE_NETTY_AVAILABLE_MEMORY_SEGMENTS = "Status.Shuffle.Netty.AvailableMemorySegments";
    public static final String STATUS_SHUFFLE_NETTY_USED_MEMORY_SEGMENTS = "Status.Shuffle.Netty.UsedMemorySegments";
    public static final String STATUS_SHUFFLE_NETTY_TOTAL_MEMORY_SEGMENTS = "Status.Shuffle.Netty.TotalMemorySegments";

    // 网络内存指标常量
    public static final String STATUS_SHUFFLE_NETTY_AVAILABLE_MEMORY = "Status.Shuffle.Netty.AvailableMemory";
    public static final String STATUS_SHUFFLE_NETTY_USED_MEMORY = "Status.Shuffle.Netty.UsedMemory";
    public static final String STATUS_SHUFFLE_NETTY_TOTAL_MEMORY = "Status.Shuffle.Netty.TotalMemory";

    public static final List<String> STATUS_ID_LIST = Arrays.asList(
            STATUS_JVM_MEMORY_HEAP_USED,STATUS_JVM_MEMORY_HEAP_MAX,
            STATUS_SHUFFLE_NETTY_USED_MEMORY, STATUS_SHUFFLE_NETTY_TOTAL_MEMORY,
            STATUS_FLINK_MEMORY_MANAGED_USED, STATUS_FLINK_MEMORY_MANAGED_TOTAL,
            STATUS_JVM_MEMORY_METASPACE_USED, STATUS_JVM_MEMORY_METASPACE_MAX);

}
