import Fields.FIELDS
import Profile.profile
import Utils.printMU
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.{
  ClassTagExtensions,
  DefaultScalaModule
}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{Consumed, KStream}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}

import java.util.Properties
import scala.collection.immutable.ArraySeq
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParVector

object EM_Scala extends LazyLogging {

  private val CFG_KAFKA_APPLICATION_ID = "AppID_EM_Scala"
  private val CFG_KAFKA_CLIENT_ID = "ClientID_EM_Scala"
  private val CFG_KAFKA_BOOTSTRAP_SERVERS = "kafka-01:9092"
  private val CFG_KAFKA_TOPIC_INTERACTION_EM = "JeMPI-interaction-em"

  private val mapper = new ObjectMapper() with ClassTagExtensions
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  private val buffer = new ArrayBuffer[Array[String]]()

  def main(args: Array[String]): Unit = {

    val props = loadConfig()
    val stringSerde: Serde[String] = Serdes.String()
    val streamsBuilder: StreamsBuilder = new StreamsBuilder()
    val patientRecordKStream: KStream[String, String] = streamsBuilder.stream(
      CFG_KAFKA_TOPIC_INTERACTION_EM,
      Consumed.`with`(stringSerde, stringSerde)
    )
    patientRecordKStream.foreach((_, json) => {
      val interactionEnvelop =
        mapper.readValue(json, classOf[InteractionEnvelop])
      interactionEnvelop.contentType match {
        case "BATCH_START_SENTINEL" => buffer.clearAndShrink()
        case "BATCH_END_SENTINEL" =>
          val parVector = new ParVector(buffer.toVector)
          buffer.clearAndShrink()
          val emRunnable: EM_Runnable = new EM_Runnable(parVector)
          val thread: Thread = new Thread(emRunnable)
          thread.start();
        case "BATCH_INTERACTION" =>
          if (interactionEnvelop.interaction.isDefined) {
            val auxId =
              interactionEnvelop.interaction.get.uniqueInteractionData.auxId
            val givenName =
              interactionEnvelop.interaction.get.demographicData.givenName
            val familyName =
              interactionEnvelop.interaction.get.demographicData.familyName
            val gender =
              interactionEnvelop.interaction.get.demographicData.gender
            val dob = interactionEnvelop.interaction.get.demographicData.dob
            val city = interactionEnvelop.interaction.get.demographicData.city
            val phoneNumber =
              interactionEnvelop.interaction.get.demographicData.phoneNumber
            val nationalId =
              interactionEnvelop.interaction.get.demographicData.nationalId
            val interaction = Array(
              auxId,
              givenName,
              familyName,
              gender,
              dob,
              city,
              phoneNumber,
              nationalId
            )
            buffer += interaction
            logger.info(
              "{} {} {} {} {} {} {} {}",
              auxId,
              givenName,
              familyName,
              gender,
              dob,
              city,
              phoneNumber,
              nationalId
            )
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

  private class EM_Runnable(val interactions: ParVector[Array[String]])
      extends Runnable {

    def run(): Unit = {
      val interactions_ : ParVector[ArraySeq[String]] =
        interactions.map((fields: Array[String]) =>
          ArraySeq.unsafeWrapArray(fields)
        )
      val (mu, ms) = profile(EM_Task.run(interactions_))
      FIELDS.zipWithIndex.foreach(x => printMU(x._1.name, mu(x._2)))
      logger.info(s"$ms ms")
    }

  }

}
