package org.jembi.jempi.em

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.{
  ClassTagExtensions,
  DefaultScalaModule
}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{Consumed, KStream}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.jembi.jempi.em.configuration.Config
import org.jembi.jempi.em.kafka.Config.{
  CFG_KAFKA_APPLICATION_ID,
  CFG_KAFKA_BOOTSTRAP_SERVERS,
  CFG_KAFKA_CLIENT_ID,
  CFG_KAFKA_TOPIC_INTERACTION_EM
}
import org.jembi.jempi.em.kafka.Producer

import java.nio.file.Paths
import java.util.Properties
import scala.collection.immutable.ArraySeq
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParVector
import scala.util.Random

object EM_Scala extends LazyLogging {

  private val objectMapper = new ObjectMapper() with ClassTagExtensions
  objectMapper.registerModule(DefaultScalaModule)
  objectMapper.configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    false
  )

  private val buffer = new ArrayBuffer[Array[String]]()

  def main(args: Array[String]): Unit = {

    val jsonMapper = JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .build() :: ClassTagExtensions

    val jsonConfig = jsonMapper.readValue(
      Paths.get("/app/conf/config-reference.json").toFile,
      new TypeReference[Config] {}
    )
    jsonConfig.demographicFields.zipWithIndex.foreach(f =>
      System.out.println(Utils.snakeCaseToCamelCase(f._1.fieldName), f._2)
    )
    val fieldsCols = ArraySeq.unsafeWrapArray(
      jsonConfig.demographicFields.zipWithIndex.map(f =>
        Field(Utils.snakeCaseToCamelCase(f._1.fieldName), f._2)
      )
    )
    val linkCols = ArraySeq.unsafeWrapArray(
      jsonConfig.demographicFields.zipWithIndex
        .filter(f => f._1.linkMetaData.isDefined)
        .map(f => f._2)
    )
    val validateCols = ArraySeq.unsafeWrapArray(
      jsonConfig.demographicFields.zipWithIndex
        .filter(f => f._1.validateMetaData.isDefined)
        .map(f => f._2)
    )
    val matchCols = ArraySeq.unsafeWrapArray(
      jsonConfig.demographicFields.zipWithIndex
        .filter(f => f._1.matchMetaData.isDefined)
        .map(f => f._2)
    )

    val fieldsConfig =
      FieldsConfig(fieldsCols, linkCols, validateCols, matchCols)

    val props = loadConfig()
    val stringSerde: Serde[String] = Serdes.String()
    val streamsBuilder: StreamsBuilder = new StreamsBuilder()
    val patientRecordKStream: KStream[String, String] = streamsBuilder.stream(
      CFG_KAFKA_TOPIC_INTERACTION_EM,
      Consumed.`with`(stringSerde, stringSerde)
    )
    patientRecordKStream.foreach((_, json) => {
      val interactionEnvelop =
        objectMapper.readValue(json, classOf[CustomInteractionEnvelop])
      interactionEnvelop.contentType match {
        case "BATCH_START_SENTINEL" => buffer.clearAndShrink()
        case "BATCH_END_SENTINEL" =>
          val parVector = new ParVector(
            if (buffer.length <= 50_000) buffer.toVector
            else Random.shuffle(buffer.toVector).take(50_000)
          )
          buffer.clearAndShrink()
          val emRunnable: EM_Runnable =
            new EM_Runnable(fieldsConfig, interactionEnvelop.tag.get, parVector)
          val thread: Thread = new Thread(emRunnable)
          thread.start()
        case "BATCH_INTERACTION" =>
          if (interactionEnvelop.interaction.isDefined) {
            val interaction =
              interactionEnvelop.interaction.get.demographicData.toArray
            buffer += interaction
          }
      }
    })
    val patientKafkaStreams: KafkaStreams =
      new KafkaStreams(streamsBuilder.build(), props)
    patientKafkaStreams.cleanUp()
    patientKafkaStreams.start()

  }

  private def loadConfig(): Properties = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, CFG_KAFKA_APPLICATION_ID)
    props.put(StreamsConfig.CLIENT_ID_CONFIG, CFG_KAFKA_CLIENT_ID)
    props.put(
      StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
      CFG_KAFKA_BOOTSTRAP_SERVERS
    )
    props
  }

  private class EM_Runnable(
      val fieldsConfig: FieldsConfig,
      val tag: String,
      val interactions: ParVector[Array[String]]
  ) extends Runnable {

    def run(): Unit = {
      val interactions_ : ParVector[ArraySeq[String]] =
        interactions.map((fields: Array[String]) =>
          ArraySeq.unsafeWrapArray(fields)
        )
      var linkResults: (ArraySeq[Probability], Double) = (null, 0.0)
      var validateResults: (ArraySeq[Probability], Double) = (null, 0.0)
      var matchResults: (ArraySeq[Probability], Double) = (null, 0.0)

      if (fieldsConfig.linkCols.length > 1) {
        linkResults = Profile.profile(
          EM_Task.run(fieldsConfig.fields, fieldsConfig.linkCols, interactions_)
        )
      }
      if (fieldsConfig.validateCols.length > 1) {
        validateResults = Profile.profile(
          EM_Task.run(
            fieldsConfig.fields,
            fieldsConfig.validateCols,
            interactions_
          )
        )
      }
      if (fieldsConfig.matchCols.length > 1) {
        matchResults = Profile.profile(
          EM_Task.run(
            fieldsConfig.fields,
            fieldsConfig.matchCols,
            interactions_
          )
        )
      }

      for (i <- fieldsConfig.linkCols.indices) {
        Utils.printMU(
          fieldsConfig.fields.apply(fieldsConfig.linkCols.apply(i)).name,
          linkResults._1(i)
        )
      }

      logger.info(s"${linkResults._2} ms")
      Producer.send(tag, linkResults._1, validateResults._1, matchResults._1);
    }

  }

}
