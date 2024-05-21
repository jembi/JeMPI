package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object CustomLinkerMU {

  private val classLocation =
    "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker"
  private val custom_className = "CustomLinkerMU"
  private val packageText = "org.jembi.jempi.linker"

  def generate(config: Config): Any = {
    val classFile: String =
      classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val muList =
      for (t <- config.demographicFields.filter(f => f.linkMetaData.isDefined))
        yield t

    writer.println(s"package $packageText;")
    writer.println()

    if (muList.length == 0) {
      writer.println(s"""public class $custom_className {
           |}
           |""".stripMargin)
    } else {
      writer.println(s"""import org.apache.commons.lang3.StringUtils;
           |import org.apache.commons.text.similarity.SimilarityScore;
           |import org.apache.commons.text.similarity.JaroWinklerSimilarity;
           |import org.apache.logging.log4j.LogManager;
           |import org.apache.logging.log4j.Logger;
           |import org.jembi.jempi.shared.models.CustomDemographicData;
           |
           |import java.util.Locale;
           |
           |public final class $custom_className {
           |
           |   private static final Logger LOGGER = LogManager.getLogger($custom_className.class);
           |   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
           |
           |   private final Fields fields = new Fields();
           |
           |   CustomLinkerMU() {
           |      LOGGER.debug("CustomLinkerMU");
           |   }
           |
           |   private static boolean fieldMismatch(
           |         final Field field,
           |         final String left,
           |         final String right) {
           |      return field.similarityScore.apply(left, right) <= field.threshold;
           |   }
           |
           |   private void updateMatchedPair(
           |         final Field field,
           |         final String left,
           |         final String right) {
           |      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(field, left, right)) {
           |         field.matchedPairFieldUnmatched += 1;
           |      } else {
           |         field.matchedPairFieldMatched += 1;
           |      }
           |   }
           |
           |   private void updateUnMatchedPair(
           |         final Field field,
           |         final String left,
           |         final String right) {
           |      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(field, left, right)) {
           |         field.unMatchedPairFieldUnmatched += 1;
           |      } else {
           |         field.unMatchedPairFieldMatched += 1;
           |      }
           |   }
           |""".stripMargin)

      writer.println(s"""   void updateMatchSums(
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
    }
    writer.flush()
    writer.close()
  }

}
