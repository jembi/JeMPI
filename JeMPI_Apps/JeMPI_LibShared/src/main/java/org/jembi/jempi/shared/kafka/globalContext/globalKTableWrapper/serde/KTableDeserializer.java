package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.jembi.jempi.shared.utils.AppUtils;



public class KTableDeserializer<T> implements Deserializer<T> {
    private final Class<T> serializeCls;
    public KTableDeserializer(Class<T> serializeCls){
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
