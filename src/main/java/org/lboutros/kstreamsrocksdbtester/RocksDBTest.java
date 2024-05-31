package org.lboutros.kstreamsrocksdbtester;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.metrics.JmxReporter;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.ExtendedTopologyTestDriver;
import org.apache.kafka.streams.processor.internals.metrics.StreamsMetricsImpl;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.internals.MeteredKeyValueStore;
import org.lboutros.kstreamsrocksdbtester.topology.SimpleRocksdbTopologySupplier;

import java.io.Closeable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.lboutros.kstreamsrocksdbtester.configuration.Utils.readConfiguration;
import static org.lboutros.kstreamsrocksdbtester.topology.SimpleRocksdbTopologySupplier.A_STORE_NAME;
import static org.lboutros.kstreamsrocksdbtester.topology.SimpleRocksdbTopologySupplier.DELETE_PREFIX;

@Slf4j
class RocksDBTest implements Closeable {
    private static final Random R = new Random();
    private static final Time TIME = new SystemTime();


    private final ExtendedTopologyTestDriver testDriver;

    private final MockProducer<byte[], byte[]> producer;

    private final StreamsMetricsImpl streamsMetrics;

    private final Metrics metrics;

    private final ScheduledExecutorService rocksDBMetricsRecordingService;

    private final MeteredKeyValueStore<String, String> stateStore;

    private long totalTime = 0;
    private long prefixScanCount = 0;

    private final JmxReporter jmxReporter;

    public RocksDBTest() throws javax.naming.ConfigurationException, ConfigurationException, IllegalAccessException, NoSuchFieldException {
        Properties streamsConfiguration = readConfiguration("test.properties");

        testDriver = new ExtendedTopologyTestDriver(
                new SimpleRocksdbTopologySupplier().get(),
                streamsConfiguration,
                Instant.EPOCH);

        stateStore = (MeteredKeyValueStore<String, String>) testDriver.<String, String>getKeyValueStore(A_STORE_NAME);

        rocksDBMetricsRecordingService = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "test-client" + "-RocksDBMetricsRecordingTrigger");
            thread.setDaemon(true);
            return thread;
        });

        jmxReporter = new JmxReporter();

        producer = testDriver.getProducer();
        metrics = testDriver.getMetrics();
        testDriver.setMockWallClockTime(new SystemTime());
        streamsMetrics = testDriver.getStreamsMetrics();

        metrics.addReporter(jmxReporter);

        final long recordingDelay = 0;
        final long recordingInterval = 1;

        rocksDBMetricsRecordingService.scheduleAtFixedRate(
                streamsMetrics.rocksDBMetricsRecordingTrigger(),
                recordingDelay,
                recordingInterval,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void close() {
        testDriver.close();
        rocksDBMetricsRecordingService.shutdown();
        jmxReporter.close();
    }

    private String generateKey() {
        int nextInt = R.nextInt(1_000);
        // TODO: Should be configurable
        if (nextInt < 10) {
            // Set 1% random lines as delete prefixed ones
            return "DELETE#" + (R.nextInt(10_000_000) + 10_000_000);
        } else {
            return Integer.toString(R.nextInt(10_000_000) + 10_000_000);
        }
    }

    public void run() throws NoSuchFieldException, IllegalAccessException {
        try (Serde<String> serde = Serdes.String()) {
            // Insert entries in the state
            // TODO: should be configurable
            for (int i = 0; i < 200_000_000; i++) {
                // TODO: should be configurable
                stateStore.put(generateKey(), generateDummyData(100));

                // TODO: should be configurable
                if (i % 10_000 == 0) {
                    long start = TIME.nanoseconds();
                    List<String> keys = new ArrayList<>();
                    try (KeyValueIterator<String, String> iterator =
                                 stateStore.prefixScan(DELETE_PREFIX, serde.serializer())) {
                        prefixScanCount++;
                        long prefixScanTime = TIME.nanoseconds() - start;
                        totalTime += prefixScanTime;
                        // TODO: should be configurable
                        if (i % 100_000 == 0) {
                            // Clear mock producer in order to prevent OOM
                            producer.clear();

                            var stateStoreMetrics = testDriver.metrics().entrySet().stream()
                                    .filter(e -> e.getKey().group().equals("stream-state-metrics")
                                            && e.getKey().tags().get("rocksdb-state-id").equals(A_STORE_NAME))
                                    .sorted(Comparator.comparing(e -> e.getKey().name()))
                                    .toList();

                            stateStoreMetrics.stream()
                                    .filter(e -> e.getKey().name().contains("hit-ratio")
                                            || e.getKey().name().contains("usage")
                                            || e.getKey().name().contains("block-cache")
                                            || e.getKey().name().contains("prefix")
                                            || e.getKey().name().contains("get")
                                            || e.getKey().name().contains("put")
                                            || e.getKey().name().contains("flush"))
                                    .forEach(e -> log.info("{}: {}", e.getKey().name(), e.getValue().metricValue()));
                            // TODO: should be configurable
                            while (iterator.hasNext() && keys.size() < 300) {
                                keys.add(iterator.next().key);
                            }
                            if (iterator.hasNext()) {
                                log.info("{}: {} entries: {} μs: {}", i, stateStore.approximateNumEntries(), prefixScanTime / 1_000, iterator.next().key);
                            }
                            log.info("{}: {} entries: Average time: {} μs", i, stateStore.approximateNumEntries(), ((double) totalTime) / (1_000 * prefixScanCount));
                        }
                    }
                    if (!keys.isEmpty()) {
                        log.info("Deleting {} keys...", keys.size());
                        keys.forEach(stateStore::delete);
                        log.info("Deleting done.");
                    }
                }
            }
        }
    }

    private String generateDummyData(int count) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            builder.append(R.nextInt(256));
        }
        return builder.toString();
    }
}