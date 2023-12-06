package org.lboutros.kstreamsrocksdbtester.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.internals.CustomRocksDbKeyValueBytesStoreSupplier;
import org.lboutros.kstreamsrocksdbtester.topology.processor.BasicProcessor;
import org.rocksdb.ReadOptions;

import java.util.function.Supplier;

public class SimpleRocksdbTopologySupplier implements Supplier<Topology> {
    public static final String A_STORE_NAME = "aStoreName";
    public static final String DELETE_PREFIX = "DELETE#";

    @Override
    public Topology get() {
        var builder = new StreamsBuilder();
        ReadOptions readOptions = new ReadOptions();
        readOptions.setAutoPrefixMode(true);

        final StoreBuilder<KeyValueStore<String, String>> store =
                Stores.keyValueStoreBuilder(
                        new CustomRocksDbKeyValueBytesStoreSupplier(A_STORE_NAME, readOptions, DELETE_PREFIX.length()),
                        Serdes.String(),
                        Serdes.String()
                );

        builder.addStateStore(store);

        builder.stream("input", Consumed.with(Serdes.String(), Serdes.String()))
                .process(BasicProcessor::new, A_STORE_NAME);
        return builder.build();
    }
}
