package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerMU {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val custom_className = "CustomLinkerMU"
  private val packageText = "org.jembi.jempi.linker"

  def parseRules(config: Config): Any = {
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val muList = for (
      t <- config.fields.filter(f => f.m.isDefined && f.u.isDefined)
    ) yield t

    writer.println(s"package $packageText;")
    writer.println()

    if (muList.length == 0) {
      writer.println(
        s"""public class $custom_className {
           |}
           |""".stripMargin)
    } else {
      writer.println(
        s"""import org.apache.commons.lang3.StringUtils;
           |import org.apache.commons.text.similarity.JaroWinklerSimilarity;
           |import org.apache.logging.log4j.LogManager;
           |import org.apache.logging.log4j.Logger;
           |import org.jembi.jempi.shared.models.CustomEntity;
           |import org.jembi.jempi.shared.models.CustomGoldenRecord;
           |
           |public class $custom_className {
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
           |   private static boolean fieldMismatch(final String left, final String right) {
           |      return JARO_WINKLER_SIMILARITY.apply(left, right) <= 0.92;
           |   }
           |
           |   private void updateMatchedPair(final Field field, final String left, final String right) {
           |      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(left, right)) {
           |         field.matchedPairFieldUnmatched += 1;
           |      } else {
           |         field.matchedPairFieldMatched += 1;
           |      }
           |   }
           |
           |   private void updateUnMatchedPair(final Field field, final String left, final String right) {
           |      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(left, right)) {
           |         field.unMatchedPairFieldUnmatched += 1;
           |      } else {
           |         field.unMatchedPairFieldMatched += 1;
           |      }
           |   }
           |""".stripMargin)

      writer.println("   void updateMatchSums(final CustomEntity customEntity, final CustomGoldenRecord " +
                       "customGoldenRecord) {")
      if (muList.nonEmpty) {
        muList.foreach(mu => {
          val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
          writer.println(
            s"      updateMatchedPair(fields.$fieldName, customEntity.$fieldName(), customGoldenRecord.$fieldName()" +
              s");")
        })
        writer.println(
          """      LOGGER.debug("{}", fields);
            |   }
            |""".stripMargin)
      }

      writer.println("   void updateMissmatchSums(final CustomEntity customEntity, final CustomGoldenRecord " +
                       "customGoldenRecord) {")
      muList.foreach(mu => {
        val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
        writer.println(
          s"      updateUnMatchedPair(fields.$fieldName, customEntity.$fieldName(), customGoldenRecord.$fieldName" +
            s"()" +
            s");")
      })
      writer.println(
        """      LOGGER.debug("{}", fields);
          |   }
          |
          |   static class Field {
          |      long matchedPairFieldMatched = 0L;
          |      long matchedPairFieldUnmatched = 0L;
          |      long unMatchedPairFieldMatched = 0L;
          |      long unMatchedPairFieldUnmatched = 0L;
          |   }
          |
          |   static class Fields {""".stripMargin)
      muList.foreach(mu => {
        val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
        writer.println(s"      final Field $fieldName = new Field();")
      })
      writer.println(
        """
          |      private float computeM(Field field) {
          |         return (float) (field.matchedPairFieldMatched)
          |              / (float) (field.matchedPairFieldMatched + field.matchedPairFieldUnmatched);
          |      }
          |
          |      private float computeU(Field field) {
          |         return (float) (field.unMatchedPairFieldMatched)
          |              / (float) (field.unMatchedPairFieldMatched + field.unMatchedPairFieldUnmatched);
          |      }
          |""".stripMargin)

      writer.println(
        """      @Override
          |      public String toString() {""".stripMargin)

      if (muList.nonEmpty) {
        val fmt = Range(0, muList.length)
          .map(x => "f" + (x + 1).toString + "(%f:%f)")
          .reduce { (accumulator, elem) => accumulator + " " + elem }
        //    println(fmt)

        writer.println(
          s"""         return String.format("$fmt",""".stripMargin)
        muList.zipWithIndex.foreach((mu, idx) => {
          val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
          writer.println(s"                              computeM($fieldName), computeU($fieldName)"
                           + (if ((idx + 1) != muList.length) "," else ");"))
        })
      }

      writer.print(
        s"""      }
           |
           |   }
           |
           |}""".stripMargin)
    }
    writer.flush()
    writer.close()
  }

}
