package org.jembi.jempi.shared.serdes;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Map;

public class JsonPojoSerializer<T> implements Serializer<T> {

   public JsonPojoSerializer() {}

   @Override
   public void configure(
         final Map<String, ?> props,
         final boolean isKey) {}

   @Override
   public byte[] serialize(
         final String topic,
         final T data) {
      if (data == null) {
         return new byte[0];
      }

      try {
         return AppUtils.OBJECT_MAPPER.writeValueAsBytes(data);
      } catch (Exception e) {
         throw new SerializationException("Error serializing JSON message", e);
      }
   }

   @Override
   public void close() {}

}
