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
  CFG_KAFKA_TOPIC_MU_CONTROLLER,
  CFG_KAFKA_TOPIC_MU_LINKER
}

import java.util.Properties
import scala.collection.immutable.ArraySeq

object Producer {

  def send(tag: String, muSeqLink: ArraySeq[MU], muSeqValidate: ArraySeq[MU], muSeqMatch: ArraySeq[MU]): Unit = {
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

    val customMU = CustomMU.fromArraySeq(tag, muSeqLink, muSeqValidate, muSeqMatch)

    val json = mapper.writeValueAsString(customMU)

    val record = new ProducerRecord(CFG_KAFKA_TOPIC_MU_CONTROLLER, "key", json)
    producer.send(record)
    producer.close()
  }

}
