package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.jembi.jempi.shared.utils.AppUtils;

public class KTableSerializer<T>  implements Serializer<T> {
    @Override
    public byte[] serialize(String topic, T data) {
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
