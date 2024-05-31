package org.apache.kafka.streams.state.internals;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.rocksdb.ReadOptions;

import java.util.function.Consumer;

public class CustomRocksDbKeyValueBytesStoreSupplier implements KeyValueBytesStoreSupplier {

    private final String name;
    private final Consumer<ReadOptions> readOptionsConfigurer;
    private final Integer prefixSize;

    public CustomRocksDbKeyValueBytesStoreSupplier(final String name,
                                                   final java.util.function.Consumer<ReadOptions> readOptionsConfigurer,
                                                   final Integer prefixSize) {
        this.name = name;
        this.readOptionsConfigurer = readOptionsConfigurer;
        this.prefixSize = prefixSize;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public KeyValueStore<Bytes, byte[]> get() {
        return new ConfigurableIteratorRocksDBStore(name,
                metricsScope(),
                readOptionsConfigurer,
                prefixSize);
    }

    @Override
    public String metricsScope() {
        return "rocksdb";
    }
}