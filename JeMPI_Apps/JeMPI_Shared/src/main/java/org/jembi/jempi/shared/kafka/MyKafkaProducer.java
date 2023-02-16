package org.jembi.jempi.shared.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public final class MyKafkaProducer<KEY_TYPE, VAL_TYPE> {

   private final String topic;
   private final Producer<KEY_TYPE, VAL_TYPE> producer;

   public MyKafkaProducer(
         final String bootstrapServers,
         final String topic_,
         final Serializer<KEY_TYPE> keySerializer,
         final Serializer<VAL_TYPE> valueSerializer,
         final String clientId) {
      final Properties properties = new Properties();
      this.topic = topic_;
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

      properties.put(ProducerConfig.RETRIES_CONFIG, 3);
      properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 100000);
      properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
      properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
      properties.put(ProducerConfig.ACKS_CONFIG, "1");
      producer = new KafkaProducer<>(properties, keySerializer, valueSerializer);
   }

   public void close() {
      producer.close();
   }

   public void initTransactions() {
      producer.initTransactions();
   }

   public void beginTransaction() {
      producer.beginTransaction();
   }

   public void commitTransaction() {
      producer.commitTransaction();
   }

   public RecordMetadata produceSync(
         final KEY_TYPE key,
         final VAL_TYPE item) throws ExecutionException,
                                     InterruptedException {
      final ProducerRecord<KEY_TYPE, VAL_TYPE> rec = new ProducerRecord<>(topic, key, item);
      return producer.send(rec).get();
   }

   public void produceAsync(
         final KEY_TYPE key,
         final VAL_TYPE item,
         final Callback callback) {
      final ProducerRecord<KEY_TYPE, VAL_TYPE> rec = new ProducerRecord<>(topic, key, item);
      producer.send(rec, callback);
   }

}
