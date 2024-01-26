package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomInteractionEnvelop {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em"
  private val custom_className = "CustomInteractionEnvelop"
  private val packageText = "org.jembi.jempi.em"

  def generate(config: Config): Any = {
    val classFile: String =
      classLocation + File.separator + custom_className + ".scala"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val muList =
      for (t <- config.demographicFields.filter(f => f.linkMetaData.isDefined))
        yield t

    writer.println(s"package $packageText")
    writer.println()

    if (muList.length == 0) {
      writer.println(s"""public class $custom_className {
           |}
           |""".stripMargin)
    } else {
      writer.println(s"""
           |import com.fasterxml.jackson.annotation.JsonIgnoreProperties
           |
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class ${custom_className}(
           |    contentType: String,
           |    tag: Option[String],
           |    stan: Option[String],
           |    interaction: Option[Interaction]
           |) {}
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class Interaction(
           |    uniqueInteractionData: UniqueInteractionData,
           |    demographicData: DemographicData
           |)
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class UniqueInteractionData(auxId: String)
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class DemographicData(
           |    givenName: String,
           |    familyName: String,
           |    gender: String,
           |    dob: String,
           |    city: String,
           |    phoneNumber: String,
           |    nationalId: String
           |)
           |""".stripMargin)

      /*      writer.println(s"""   void updateMatchSums(
           |         final CustomDemographicData patient,
           |         final CustomDemographicData goldenRecord) {""".stripMargin)
      if (muList.nonEmpty) {
        muList.foreach(mu => {
          val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
          writer.println(
            s"      updateMatchedPair(fields.$fieldName, patient.$fieldName, goldenRecord.$fieldName" +
              s");"
          )
        })
        writer.println("""      LOGGER.debug("{}", fields);
            |   }
            |""".stripMargin)
      }

      writer.println(s"""   void updateMissmatchSums(
           |         final CustomDemographicData patient,
           |         final CustomDemographicData goldenRecord) {""".stripMargin)
      muList.foreach(mu => {
        val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
        writer.println(
          s"      updateUnMatchedPair(fields.$fieldName, patient.$fieldName, goldenRecord.$fieldName);"
        )
      })
      writer.println("""      LOGGER.debug("{}", fields);
          |   }
          |
          |   static class Field {
          |      final SimilarityScore<Double> similarityScore;
          |      final double threshold;
          |      long matchedPairFieldMatched = 0L;
          |      long matchedPairFieldUnmatched = 0L;
          |      long unMatchedPairFieldMatched = 0L;
          |      long unMatchedPairFieldUnmatched = 0L;
          |
          |      Field(final SimilarityScore<Double> score,
          |            final double mismatchThreshold) {
          |         this.similarityScore = score;
          |         this.threshold = mismatchThreshold;
          |      }
          |   }
          |
          |   static class Fields {""".stripMargin)
      muList.foreach(mu => {
        val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
        writer.println(
          s"      final Field $fieldName = new Field(JARO_WINKLER_SIMILARITY, 0.92);"
        )
      })
      writer.println("""
          |      private float computeM(final Field field) {
          |         return (float) (field.matchedPairFieldMatched)
          |              / (float) (field.matchedPairFieldMatched + field.matchedPairFieldUnmatched);
          |      }
          |
          |      private float computeU(final Field field) {
          |         return (float) (field.unMatchedPairFieldMatched)
          |              / (float) (field.unMatchedPairFieldMatched + field.unMatchedPairFieldUnmatched);
          |      }
          |""".stripMargin)

      writer.println("""      @Override
          |      public String toString() {""".stripMargin)

      if (muList.nonEmpty) {
        val fmt = Range(0, muList.length)
          .map(x => "f" + (x + 1).toString + "(%f:%f)")
          .reduce { (accumulator, elem) => accumulator + " " + elem }
        //    println(fmt)

        writer.println(
          s"""         return String.format(Locale.ROOT, "$fmt",""".stripMargin
        )
        muList.zipWithIndex.foreach((mu, idx) => {
          val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
          writer.println(
            s"                              computeM($fieldName), computeU($fieldName)"
              + (if ((idx + 1) != muList.length) "," else ");")
          )
        })
      }

      writer.println(s"""      }
           |
           |   }
           |
           |}""".stripMargin)
       */
    }
    writer.flush()
    writer.close()
  }

}
