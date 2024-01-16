package org.jembi.jempi.em.kafka

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.{
  ClassTagExtensions,
  DefaultScalaModule
}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.jembi.jempi.em.MU
import org.jembi.jempi.em.kafka.Config.{
  CFG_KAFKA_BOOTSTRAP_SERVERS,
  CFG_KAFKA_TOPIC_MU_LINKER
}

import java.util.Properties
import scala.collection.immutable.ArraySeq

object Producer {

  def send(tag: String, muSeq: ArraySeq[MU]): Unit = {
    val mapper = new ObjectMapper() with ClassTagExtensions
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val props = new Properties()
    props.put("bootstrap.servers", CFG_KAFKA_BOOTSTRAP_SERVERS)
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )

    val producer = new KafkaProducer[String, String](props)

    val customMU = CustomMU(
      tag,
      Probability(muSeq.apply(0).m, muSeq.apply(0).u),
      Probability(muSeq.apply(1).m, muSeq.apply(1).u),
      Probability(muSeq.apply(2).m, muSeq.apply(2).u),
      Probability(muSeq.apply(3).m, muSeq.apply(3).u),
      Probability(muSeq.apply(4).m, muSeq.apply(4).u),
      Probability(muSeq.apply(5).m, muSeq.apply(5).u),
      Probability(muSeq.apply(6).m, muSeq.apply(6).u)
    )
    val json = mapper.writeValueAsString(customMU)

    val record = new ProducerRecord(CFG_KAFKA_TOPIC_MU_LINKER, "key", json)
    producer.send(record)
    producer.close()
  }

}
