package org.jembi.jempi.shared.kafka.global_context.store_processor.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.jembi.jempi.shared.utils.AppUtils;

public final class StoreValueSerializer<T>  implements Serializer<T> {
    @Override
    public byte[] serialize(final String topic, final T data) {
        if (data == null) {
            return new byte[0];
        }

        try {
            return AppUtils.OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }
}
