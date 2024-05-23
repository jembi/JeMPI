package org.jembi.jempi.em

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.{
  ClassTagExtensions,
  DefaultScalaModule
}
import com.typesafe.scalalogging.LazyLogging
import org.jembi.jempi.em.Utils.printMU
import org.jembi.jempi.em.configuration.Config

import java.nio.file.Paths
import scala.collection.immutable.ArraySeq
import scala.collection.parallel.immutable.ParVector

object EM_Scala extends LazyLogging {

  private val CSV_FILE =
    "./test-data-0005035-0001000-05-50.csv" //    4317 ms   2.935625434329395e6 / sec
//    "./test-data-0020041-0004000-05-50.csv" //   29697 ms   6.761990100010103e6 / sec
//    "./test-data-0049995-0010000-05-50.csv"  //  112601 ms   1.109870263141535e7 / sec
//    "./test-data-0099940-0020000-05-50.csv"  //  405032 ms   1.232977105512651e7 / sec
//    "./test-data-0199916-0040000-05-50.csv"  // 1462610 ms   1.366263294384696e7 / sec
//    "./test-data-0500150-0100000-05-50.csv"  // 8700260 ms   1.437597970348013e7 / sec

  private val objectMapper = new ObjectMapper() with ClassTagExtensions
  objectMapper.registerModule(DefaultScalaModule)
  objectMapper.configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    false
  )

  def main(args: Array[String]): Unit = {

    val jsonMapper = JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .build() :: ClassTagExtensions

    val jsonConfig = jsonMapper.readValue(
      Paths
        .get(
          "src/main/resources/config-reference-link-dp.json"
        )
        .toFile,
      new TypeReference[Config] {}
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

    val interactions_ : ParVector[Array[String]] =
      ReadCSV.readCsv(fieldsConfig.fields, CSV_FILE)
    val interactions: ParVector[ArraySeq[String]] =
      interactions_.map((fields: Array[String]) =>
        ArraySeq
          .unsafeWrapArray(fields)
      )
    val emRunnable: EM_Runnable = new EM_Runnable(fieldsConfig, interactions)
    val thread: Thread = new Thread(emRunnable)
    thread.start()
    thread.join()
  }

  private class EM_Runnable(
      val fieldsConfig: FieldsConfig,
      interactions: ParVector[ArraySeq[String]]
  ) extends Runnable {

    def run(): Unit = {
      var t1: (ArraySeq[Probability], Double) = (null, 0.0)
      var t2: (ArraySeq[Probability], Double) = (null, 0.0)
      var t3: (ArraySeq[Probability], Double) = (null, 0.0)
      if (fieldsConfig.linkCols.length > 1) {
        t1 = Profile.profile(
          EM_Task.run(fieldsConfig.fields, fieldsConfig.linkCols, interactions)
        )
      }
      if (fieldsConfig.validateCols.length > 1) {
        t2 = Profile.profile(
          EM_Task.run(
            fieldsConfig.fields,
            fieldsConfig.validateCols,
            interactions
          )
        )
      }
      if (fieldsConfig.matchCols.length > 1) {
        t3 = Profile.profile(
          EM_Task.run(fieldsConfig.fields, fieldsConfig.matchCols, interactions)
        )
      }

      if (t1._1 != null) {
        logger.info("")
        for (i <- fieldsConfig.linkCols.indices) {
          printMU(
            fieldsConfig.fields.apply(fieldsConfig.linkCols.apply(i)).name,
            t1._1(i)
          )
        }
      }
      if (t2._1 != null) {
        logger.info("")
        for (i <- fieldsConfig.validateCols.indices) {
          printMU(
            fieldsConfig.fields.apply(fieldsConfig.validateCols.apply(i)).name,
            t2._1(i)
          )
        }
      }
      if (t3._1 != null) {
        logger.info("")
        for (i <- fieldsConfig.matchCols.indices) {
          printMU(
            fieldsConfig.fields.apply(fieldsConfig.matchCols.apply(i)).name,
            t3._1(i)
          )
        }
      }

      logger.info(s"${t1._2}ms")
      logger.info(s"${t2._2}ms")
      logger.info(s"${t3._2}ms")
    }

  }

}
