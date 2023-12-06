package org.apache.kafka.streams.state.internals;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.rocksdb.ReadOptions;

public class CustomRocksDbKeyValueBytesStoreSupplier implements KeyValueBytesStoreSupplier {

    private final String name;
    private Integer prefixSize;
    private ReadOptions readOptions;

    public CustomRocksDbKeyValueBytesStoreSupplier(final String name,
                                                   final ReadOptions readOptions,
                                                   final Integer prefixSize) {
        this.name = name;
        this.prefixSize = prefixSize;
        this.readOptions = readOptions;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public KeyValueStore<Bytes, byte[]> get() {
        return new ConfigurableIteratorRocksDBStore(name, metricsScope(), readOptions, prefixSize);
    }

    @Override
    public String metricsScope() {
        return "rocksdb";
    }
}