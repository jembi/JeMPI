package org.jembi.jempi.shared.kafka.global_context.store_processor.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.jembi.jempi.shared.utils.AppUtils;

public class StoreValueDeserializer<T> implements Deserializer<T> {
    private final Class<T> serializeCls;
    public StoreValueDeserializer(Class<T> serializeCls){
        this.serializeCls = serializeCls;
    }
    @Override
    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        T data = null;
        try {
            data = AppUtils.OBJECT_MAPPER.readValue(bytes,  this.serializeCls);
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
        return data;
    }
}
