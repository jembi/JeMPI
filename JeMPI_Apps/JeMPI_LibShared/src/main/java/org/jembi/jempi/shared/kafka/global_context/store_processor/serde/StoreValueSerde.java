package org.jembi.jempi.shared.kafka.global_context.store_processor.serde;

import org.apache.kafka.common.serialization.Serdes;

public class StoreValueSerde<T> extends Serdes.WrapperSerde<T> {
    public StoreValueSerde(final Class<T> serializeCls) {
        super(new StoreValueSerializer<>(), new StoreValueDeserializer<>(serializeCls));
    }
}
