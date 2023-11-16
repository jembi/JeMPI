package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde;

import org.apache.kafka.common.serialization.Serdes;

public class KTableSerde<T> extends Serdes.WrapperSerde<T> {

    public KTableSerde(Class<T> serializeCls) {
        super(new KTableSerializer<T>(), new KTableDeserializer<T>(serializeCls));
    }
}