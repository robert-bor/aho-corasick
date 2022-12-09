package com.intendia.jmh;

import static java.lang.Double.NaN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openjdk.jmh.results.AggregationPolicy.AVG;
import static org.openjdk.jmh.results.AggregationPolicy.MAX;
import static org.openjdk.jmh.results.AggregationPolicy.SUM;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;
import org.openjdk.jmh.util.HashMultiset;
import org.openjdk.jmh.util.Multiset;

/**
 * https://cruftex.net/2017/03/28/The-6-Memory-Metrics-You-Should-Track-in-Your-Java-Benchmarks.html
 * https://github.com/cache2k/cache2k-benchmark/blob/094baada3248f2ac759bd0803b74d6dc77f33274/jmh-suite/src/main/java/org/cache2k/benchmark/jmh/GcProfiler.java
 *
 * <b>NOTE</b>: since java9 you must add this argument to allow jdk.management access
 * <pre>
 *     --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
 * </pre>
 *
 * TODO configure stats enum using benchmark params
 */
public class GcProfiler implements InternalProfiler {
    static final String PREFIX = "mem.";

    enum Metric {ALLOC, COLLECT, POOLS}

    private EnumSet<Metric> stats = EnumSet.of(Metric.ALLOC);
    private MyHook hook;

    private long beforeT;
    private long beforeGcTime;
    private long beforeGcCount;
    private HotspotAllocationSnapshot beforeAlloc;

    @Override public String getDescription() { return "GC profiling via standard MBeans";}

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        if (stats.contains(Metric.POOLS)) installHooks();

        long gcTime = 0, gcCount = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcTime += bean.getCollectionTime(); gcCount += bean.getCollectionCount();
        }

        this.beforeT = System.nanoTime();
        this.beforeGcTime = gcTime;
        this.beforeGcCount = gcCount;
        this.beforeAlloc = HotspotAllocationSnapshot.create();
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams bParams, IterationParams iParams,
                                                       IterationResult iResult) {
        try { Thread.sleep(500);} catch (InterruptedException ignore) {}

        long afterT = System.nanoTime();
        MyHook pool = stats.contains(Metric.POOLS) ? uninstallHooks() : null;
        HotspotAllocationSnapshot newSnapshot = HotspotAllocationSnapshot.create();

        long gcTime = 0, gcCount = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcCount += bean.getCollectionCount();
            gcTime += bean.getCollectionTime();
        }

        Stream.Builder<Result> results = Stream.builder();

        if (stats.contains(Metric.ALLOC)) {
            if (beforeAlloc == HotspotAllocationSnapshot.EMPTY) {
                results.add(new ScalarResult(PREFIX + "alloc.rate", NaN, "MB/sec", AVG));
                results.add(new ScalarResult(PREFIX + "alloc.norm", NaN, "B/op", AVG));
            } else {
                long alloc = newSnapshot.subtract(beforeAlloc);
                long allOps = iResult.getMetadata().getAllOps();
                double rate = afterT == beforeT ? NaN :
                        1.0 * alloc / 1024 / 1024 * SECONDS.toNanos(1) / (afterT - beforeT);
                double norm = alloc == 0 ? 0 : allOps != 0 ? 1.0 * alloc / allOps : NaN;
                results.add(new ScalarResult(PREFIX + "alloc.rate", rate, "MB/sec", AVG));
                results.add(new ScalarResult(PREFIX + "alloc.norm", norm, "B/op", AVG));
            }
        }

        if (stats.contains(Metric.COLLECT)) {
            results.add(new ScalarResult(PREFIX + "collect.count", gcCount - beforeGcCount, "counts", SUM));
            results.add(new ScalarResult(PREFIX + "collect.time", gcTime - beforeGcTime, "ms", SUM));
        }

        if (pool != null) {
            for (String space : pool.usedDiffByPool.keys()) {
                String name = space.replaceAll(" ", "_");
                double rate = afterT == beforeT ? NaN :
                        1.0 * pool.usedDiffByPool.count(space) * SECONDS.toNanos(1) / (afterT - beforeT) / 1024 / 1024;
                double norm = 1.0 * pool.usedDiffByPool.count(space) / iResult.getMetadata().getAllOps();
                results.add(new ScalarResult(PREFIX + "pool." + name + ".rate", rate, "MB/sec", AVG));
                results.add(new ScalarResult(PREFIX + "pool." + name + ".norm", norm, "B/op", AVG));
            }
            if (!pool.usedAfterGc.isEmpty()) {
                Collections.sort(pool.usedAfterGc);
                long maxUag = pool.usedAfterGc.get(pool.usedAfterGc.size() - 1);
                results.add(new ScalarResult(PREFIX + "pool.all.maxUsed", maxUag, "bytes", MAX));
            }
            if (!pool.committedAfterGc.isEmpty()) {
                Collections.sort(pool.committedAfterGc);
                long maxCag = pool.committedAfterGc.get(pool.committedAfterGc.size() - 1);
                results.add(new ScalarResult(PREFIX + "pool.all.maxCommitted", maxCag, "bytes", MAX));
            }
        }

        return results.build().collect(Collectors.toList());
    }

    private synchronized void installHooks() {
        if (hook != null) throw new IllegalStateException("hook already installed");
        MyHook hook = (this.hook = new MyHook());
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            ((NotificationEmitter) bean).addNotificationListener(hook, null, null);
        }
    }

    private synchronized MyHook uninstallHooks() {
        if (hook == null) throw new IllegalStateException("hook already uninstalled or never installed");
        MyHook hook = this.hook; this.hook = null;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            try {
                ((NotificationEmitter) bean).removeNotificationListener(hook);
            } catch (ListenerNotFoundException ignore) {}
        }
        return hook;
    }

    static class HotspotAllocationSnapshot {
        public final static HotspotAllocationSnapshot EMPTY = new HotspotAllocationSnapshot(new long[0], new long[0]);

        private static volatile Method GET_THREAD_ALLOCATED_BYTES;
        private static volatile boolean allocationNotAvailable;

        private final long[] threadIds;
        private final long[] allocatedBytes;

        private HotspotAllocationSnapshot(long[] threadIds, long[] allocatedBytes) {
            this.threadIds = threadIds;
            this.allocatedBytes = allocatedBytes;
        }

        /**
         * Takes a snapshot of thread allocation counters.
         * The method might allocate, however it is assumed that allocations made by "current thread" will
         * be excluded from the result while performing {@link HotspotAllocationSnapshot#subtract(HotspotAllocationSnapshot)}
         *
         * @return snapshot of thread allocation counters
         */
        public static HotspotAllocationSnapshot create() {
            Method getBytes = getAllocatedBytesGetter();
            if (getBytes == null) return HotspotAllocationSnapshot.EMPTY;

            try {
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                long[] threadIds = threadMXBean.getAllThreadIds();
                long[] allocatedBytes = (long[]) getBytes.invoke(threadMXBean, (Object) threadIds);
                return new HotspotAllocationSnapshot(threadIds, allocatedBytes);
            } catch (IllegalAccessException | InvocationTargetException ignore) {}

            return HotspotAllocationSnapshot.EMPTY;
        }

        /**
         * Estimates allocated bytes based on two snapshots.
         * The problem is threads can come and go while performing the benchmark,
         * thus we would miss allocations made in a thread that was created and died between the snapshots.
         * <p/>
         * <p>Current thread is intentionally excluded since it believed to execute jmh infrastructure code only.
         *
         * @return estimated number of allocated bytes between profiler calls
         */
        public long subtract(HotspotAllocationSnapshot other) {
            HashMap<Long, Integer> prevIndex = new HashMap<>();
            for (int i = 0; i < other.threadIds.length; i++) {
                long id = other.threadIds[i];
                prevIndex.put(id, i);
            }
            long currentThreadId = Thread.currentThread().getId();
            long allocated = 0;
            for (int i = 0; i < threadIds.length; i++) {
                long id = threadIds[i];
                if (id == currentThreadId) {
                    continue;
                }
                allocated += allocatedBytes[i];
                Integer prev = prevIndex.get(id);
                if (prev != null) {
                    allocated -= other.allocatedBytes[prev];
                }
            }
            return allocated;
        }

        private static Method getAllocatedBytesGetter() {
            Method getBytes = GET_THREAD_ALLOCATED_BYTES;
            if (getBytes != null || allocationNotAvailable) {
                return getBytes;
            }
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            try {
                getBytes = threadMXBean.getClass().getMethod("getThreadAllocatedBytes", long[].class);
                getBytes.setAccessible(true);
            } catch (Throwable e) { // To avoid jmh failure in case of incompatible JDK and/or inaccessible method
                getBytes = null;
                allocationNotAvailable = true;
                System.out.println("Allocation profiling is not available: " + e.getMessage());
            }
            GET_THREAD_ALLOCATED_BYTES = getBytes;
            return getBytes;
        }
    }

    private static class MyHook implements NotificationListener {
        final Class<?> infoType;
        final Object GC_TYPE;
        final Method infoMethod;
        final Method getGcInfo;
        final Method memoryUsageBeforeGc;
        final Method memoryUsageAfterGc;

        final Multiset<String> usedDiffByPool = new HashMultiset<>();
        final List<Long> usedAfterGc = Collections.synchronizedList(new ArrayList<>());
        final List<Long> committedAfterGc = Collections.synchronizedList(new ArrayList<>());

        public MyHook() {
            try {
                infoType = Class.forName("com.sun.management.GarbageCollectionNotificationInfo");
                GC_TYPE = infoType.getField("GARBAGE_COLLECTION_NOTIFICATION").get(null);
                infoMethod = infoType.getMethod("from", CompositeData.class);
                getGcInfo = infoType.getMethod("getGcInfo");
                memoryUsageBeforeGc = getGcInfo.getReturnType().getMethod("getMemoryUsageBeforeGc");
                memoryUsageAfterGc = getGcInfo.getReturnType().getMethod("getMemoryUsageAfterGc");
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("unexpected reflection error, maybe a non sun friendly jdk?", e);
            }

        }

        @Override
        public void handleNotification(Notification n, Object o) {
            if (!n.getType().equals(GC_TYPE)) return;
            try {
                Object gc = getGcInfo.invoke(infoMethod.invoke(null, n.getUserData()));
                @SuppressWarnings("unchecked") Map<String, MemoryUsage> mapBefore =
                        (Map<String, MemoryUsage>) memoryUsageBeforeGc.invoke(gc);
                @SuppressWarnings("unchecked") Map<String, MemoryUsage> mapAfter =
                        (Map<String, MemoryUsage>) memoryUsageAfterGc.invoke(gc);
                long committed = 0, used = 0;
                for (Map.Entry<String, MemoryUsage> entry : mapAfter.entrySet()) {
                    String name = entry.getKey();
                    MemoryUsage after = entry.getValue();
                    committed += after.getCommitted();
                    used += after.getUsed();
                    MemoryUsage before = mapBefore.get(name);
                    usedDiffByPool.add(name, before.getUsed() - after.getUsed());
                }
                usedAfterGc.add(used);
                committedAfterGc.add(committed);
            } catch (IllegalAccessException | InvocationTargetException ignore) {}
        }
    }

}