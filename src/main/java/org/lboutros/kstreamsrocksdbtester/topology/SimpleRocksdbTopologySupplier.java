package org.lboutros.kstreamsrocksdbtester.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.lboutros.kstreamsrocksdbtester.topology.processor.BasicProcessor;

import java.util.function.Supplier;

public class SimpleRocksdbTopologySupplier implements Supplier<Topology> {
    public static final String A_STORE_NAME = "aStoreName";

    @Override
    public Topology get() {
        var builder = new StreamsBuilder();

        final StoreBuilder<KeyValueStore<String, String>> store =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(A_STORE_NAME),
                        Serdes.String(),
                        Serdes.String()
                );

        builder.addStateStore(store);

        builder.stream("input", Consumed.with(Serdes.String(), Serdes.String()))
                .process(BasicProcessor::new, A_STORE_NAME);
        return builder.build();
    }
}
