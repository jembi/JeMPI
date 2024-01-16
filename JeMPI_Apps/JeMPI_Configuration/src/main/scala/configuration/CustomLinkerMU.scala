package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object CustomLinkerMU {

  private val classLocation =
    "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker"
  private val custom_className = "CustomLinkerMU"
  private val packageText = "org.jembi.jempi.linker"

  private val indent = 3

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
           |import org.jembi.jempi.linker.backend.LinkerProbabilistic;
           |
           |import java.util.Arrays;
           |import java.util.List;
           |import java.util.Map;
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
           """.stripMargin)
      
      writer.println(
      s"""${" " * indent * 1}public static final class FieldMatchInfo {""".stripMargin)

      if (muList.nonEmpty) {
        muList.zipWithIndex.foreach((mu, idx) => {
            val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
            writer.println(s"""${" " * indent * 2}LinkerProbabilistic.FieldScoreInfo $fieldName = null;""".stripMargin)
        })
      }

      // constructor
      writer.println(s"""${" " * indent * 2}public FieldMatchInfo(final CustomDemographicData patient,
                                                    final CustomDemographicData goldenRecord) {""".stripMargin)

      if (muList.nonEmpty) {
        muList.zipWithIndex.foreach((mu, idx) => {
            val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
            writer.println(s"""${" " * indent * 3}this.$fieldName = LinkerProbabilistic.fieldScoreInfo(patient.$fieldName, goldenRecord.$fieldName, LINKER_FIELDS.get("$fieldName"));""".stripMargin)
        })
      }     
       
      writer.println(s"""${" " * indent * 2}}""".stripMargin) 

      
      //toMap
      writer.println()
      writer.println(s"""${" " * indent * 2}public Map<String, LinkerProbabilistic.FieldScoreInfo> toMap() {
                       | ${" " * indent * 3}return Map.ofEntries(""".stripMargin)

      if (muList.nonEmpty) {
        muList.zipWithIndex.foreach((mu, idx) => {
            val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
            writer.println(
              s"""${" " * indent * 4}Map.entry("$fieldName", this.$fieldName)${if (idx < muList.length - 1) ',' else ""}""".stripMargin)
        })
      }     

      writer.println(s"""${" " * indent * 3});
                      | ${" " * indent * 2}} 
                      | ${" " * indent * 1}}""".stripMargin)   


      writer.println(
        s"""${" " * indent * 1}public static final Map<String, LinkerProbabilistic.Field> LINKER_FIELDS = Map.ofEntries(""".stripMargin)
        if (muList.nonEmpty) {
        muList.zipWithIndex.foreach((mu, idx) => {

          val fieldName = Utils.snakeCaseToCamelCase(mu.fieldName)
          val comparison = mu.linkMetaData.get.comparison
          val comparisonLevels = mu.linkMetaData.get.comparisonLevels
          val m: Double = mu.linkMetaData.get.m 
          val u: Double = mu.linkMetaData.get.u

          def extractComparisonList(levels: List[Double]): String = levels.map(level => s""" ${level.toString}F""".stripMargin).mkString(",").trim

          writer.println(
              s"""${" " * indent * 2}Map.entry("$fieldName",  new LinkerProbabilistic.Field($comparison, ${if (comparisonLevels.length == 1) "List.of(" else "Arrays.asList("}${extractComparisonList(comparisonLevels)}), ${m}F, ${u}F))${if (idx < muList.length - 1) ',' else ""}""".stripMargin)
        })
      }  

      writer.println(s"""${" " * indent * 1});""".stripMargin);

      writer.println(s"""}""".stripMargin)                                     


    }
    writer.flush()
    writer.close()
  }

}
