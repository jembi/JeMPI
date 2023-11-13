package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.jembi.jempi.shared.utils.AppUtils;

public class KTableDeserializer<T>  implements Deserializer<T> {
    @Override
    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        T data;
        try {
            data = AppUtils.OBJECT_MAPPER.readValue(bytes, toClazz);
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
        return data;
    }
}
