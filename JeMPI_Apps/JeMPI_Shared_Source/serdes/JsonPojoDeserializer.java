package org.jembi.jempi.shared.serdes;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Map;

public record JsonPojoDeserializer<T>(Class<T> toClazz) implements Deserializer<T> {
   private static final Logger LOGGER = LogManager.getLogger(JsonPojoDeserializer.class);

   @Override
   public void configure(Map<String, ?> props, boolean isKey) {
   }

   @Override
   public T deserialize(String topic, byte[] bytes) {
      if (bytes == null)
         return null;
      T data;
      try {
         data = AppUtils.OBJECT_MAPPER.readValue(bytes, toClazz);
      } catch (Exception ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
         throw new SerializationException(ex);
      }
      return data;
   }

   @Override
   public void close() {
   }

}
