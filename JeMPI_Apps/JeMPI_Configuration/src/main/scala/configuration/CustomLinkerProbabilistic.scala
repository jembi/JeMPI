package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object CustomLinkerProbabilistic {

  private val classLocation = "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker/backend"
  private val custom_className = "CustomLinkerProbabilistic"
  private val packageText = "org.jembi.jempi.linker.backend"

  def generate(config: Config): Any = {

    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val linkMuList = for (
      t <- config.demographicFields.filter(f => f.linkMetaData.isDefined)
    ) yield t

    val validateMuList = for (
      t <- config.demographicFields.filter(f => f.validateMetaData.isDefined)
    ) yield t

    val matchNotificationMuList = for (
      t <- config.demographicFields.filter(f => f.matchMetaData.isDefined)
    ) yield t

    writer.println(s"""package $packageText;""")
    writer.println(
      s"""
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.CustomMU;
         |
         |import java.util.Arrays;
         |import java.util.List;
         |
         |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.EXACT_SIMILARITY;
         |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JACCARD_SIMILARITY;
         |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_SIMILARITY;
         |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_WINKLER_SIMILARITY;
         |
         |public final class $custom_className {
         |
         |   static final int METRIC_MIN = 0;
         |   static final int METRIC_MAX = 1;
         |   static final int METRIC_SCORE = 2;
         |   static final int METRIC_MISSING_PENALTY = 3;
         |   static final boolean PROBABILISTIC_DO_LINKING = ${
            if (config.demographicFields.exists(x => x.linkMetaData.isDefined)) "true"
            else "false"};
         |   static final boolean PROBABILISTIC_DO_VALIDATING = ${if (config.demographicFields.exists(x => x.validateMetaData.isDefined)) "true" else "false"};
         |   static final boolean PROBABILISTIC_DO_MATCHING = ${if (config.demographicFields.exists(x => x.matchMetaData.isDefined)) "true" else "false"};
         |
         |${if (linkMuList.isEmpty)"" else "   static LinkFields updatedLinkFields = null;"}
         |${if (validateMuList.isEmpty) "" else "   static ValidateFields updatedValidateFields = null;"}
         |${if (matchNotificationMuList.isEmpty) "" else "   static MatchNotificationFields updatedMatchNotificationFields = null;"}
         |
         |   private $custom_className() {
         |   }
         |""".stripMargin)

    generateGetMU()
    if (!linkMuList.isEmpty) {
      generateFieldsRecord("LinkFields", linkMuList)
    }
    if (!validateMuList.isEmpty) {
      generateFieldsRecord("ValidateFields", validateMuList)
    }
    if (!matchNotificationMuList.isEmpty) {
      generateFieldsRecord("MatchNotificationFields", matchNotificationMuList)
    }

    if (!linkMuList.isEmpty) {
      generateCurrentFields("LinkFields", "currentLinkFields", true, linkMuList)
    }
    if (!validateMuList.isEmpty) {
      generateCurrentFields("ValidateFields", "currentValidateFields", false, validateMuList)
    }
    if (!matchNotificationMuList.isEmpty) {
      generateCurrentFields("MatchNotificationFields", "currentMatchNotificationFields", false, matchNotificationMuList)
    }

    linkProbabilisticScore()
    validateProbabilisticScore()
    matchNotificationProbabilisticScore()
    updateMU()

    writer.println("}")
    writer.flush()
    writer.close()


    def generateGetMU(): Unit =
      if (!linkMuList.isEmpty)
        writer.println("   static CustomMU getMU() {")
        writer.println("      return new CustomMU(")
        linkMuList.zipWithIndex.foreach((mu, idx) =>
          writer.print(" " * 9 + s"LinkerProbabilistic.getProbability(currentLinkFields.${
            Utils.snakeCaseToCamelCase(mu.fieldName)})")
          if (idx + 1 < linkMuList.length)
            writer.println(",")
          else
            writer.println(
              """);
                |   }
                |""".stripMargin)
          end if
        )
      end if
    end generateGetMU

    def generateFieldsRecord(recordName: String, demographicFields: Array[DemographicField]): Unit =
      writer.println(s"   private record $recordName(")
      demographicFields.zipWithIndex.foreach((mu, idx) =>
        writer.print(s"""${" " * 9}LinkerProbabilistic.Field ${Utils.snakeCaseToCamelCase(mu.fieldName)}""")
        if (idx + 1 < demographicFields.length)
          writer.println(",")
        else
          writer.println(
            s""") {
               |   }""".stripMargin)
          writer.println()
        end if
      )
    end generateFieldsRecord

    def generateCurrentFields(recordName: String,
                              varName: String,
                              linking: Boolean,
                              demographicFields: Array[DemographicField]): Unit =
      writer.print(
        s"""   static $recordName $varName =
           |      new $recordName(
           |         """.stripMargin)
      var margin = 0
      demographicFields.zipWithIndex.foreach((field, idx) =>
        if ((linking && field.linkMetaData.isDefined) || (!linking && field.validateMetaData.isDefined))
          val comparison = if (linking) field.linkMetaData.get.comparison else field.validateMetaData.get.comparison
          val comparisonLevels = if (linking) field.linkMetaData.get.comparisonLevels
                                 else field.validateMetaData.get.comparisonLevels
          val m: Double = if (linking) field.linkMetaData.get.m else field.validateMetaData.get.m
          val u: Double = if (linking) field.linkMetaData.get.u else field.validateMetaData.get.u

          def extractComparisonList(levels: List[Double]): String = levels.map(level => s""" ${
            level.toString}F""".stripMargin).mkString(",").trim

          writer.print(" " * margin + s"new LinkerProbabilistic.Field($comparison, ${
            if (comparisonLevels.length == 1) "List.of(" else "Arrays.asList("
          }${extractComparisonList(comparisonLevels)}), ${m}F, ${u}F)")
          if (idx + 1 < demographicFields.length)
            writer.println(",")
            margin = 9
          else
            writer.println(");")
            writer.println()
          end if
        end if
      )
    end generateCurrentFields

    def linkProbabilisticScore(): Unit =
      writer.println(
        """   static float linkProbabilisticScore(
          |         final CustomDemographicData goldenRecord,
          |         final CustomDemographicData interaction) {""".stripMargin)
      if (linkMuList.isEmpty)
        writer.println("""      return 0.0F;""".stripMargin)
      else
        writer.println(
          """      // min, max, score, missingPenalty
            |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
      end if
      linkMuList.zipWithIndex.foreach((field, _) =>
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(" " * 6 + "LinkerProbabilistic.updateMetricsForStringField(metrics,")
        writer.println(" " * 54 + s"goldenRecord.$fieldName, interaction.$fieldName, currentLinkFields" +
          s".$fieldName);"))
      if (!linkMuList.isEmpty)
        writer.println(
          s"""${" " * 6}return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
             |""".stripMargin)
      end if
      writer.println(
        s"""${" " * 3}}
           |""".stripMargin)
    end linkProbabilisticScore

    def validateProbabilisticScore(): Unit =
      writer.println(
        """   public static float validateProbabilisticScore(
          |         final CustomDemographicData goldenRecord,
          |         final CustomDemographicData interaction) {""".stripMargin)

      if (validateMuList.isEmpty)
        writer.println("      return 0.0F;")
      else
        writer.println(
          """
            |      // min, max, score, missingPenalty
            |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
        validateMuList.foreach(field => {
          val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
          writer.println(" " * 6 + "LinkerProbabilistic.updateMetricsForStringField(metrics,")
          writer.println(" " * 54 + s"goldenRecord.$fieldName, interaction.$fieldName, currentValidateFields" +
            s".$fieldName);")
        })
        writer.print(
          s"""${" " * 6}return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
             |""".stripMargin)
      end if
      writer.println("   }")
    end validateProbabilisticScore

    def matchNotificationProbabilisticScore(): Unit =
      writer.println(
        """
          |   static float matchNotificationProbabilisticScore(
          |         final CustomDemographicData goldenRecord,
          |         final CustomDemographicData interaction) {""".stripMargin)
      if (matchNotificationMuList.isEmpty)
        writer.println("      return 0.0F;")
      else
        writer.println(
          """
            |      // min, max, score, missingPenalty
            |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
        matchNotificationMuList.foreach(field =>
          val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
          writer.println(" " * 6 + "LinkerProbabilistic.updateMetricsForStringField(metrics,")
          writer.println(" " * 54 + s"goldenRecord.$fieldName, interaction.$fieldName, currentMatchNotificationFields" +
            s".$fieldName);")
        )
        writer.print(
          s"""${" " * 6}return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
             |""".stripMargin)
      end if
      writer.println("   }")
    end matchNotificationProbabilisticScore

    def updateMU(): Unit =
      writer.println("   public static void updateMU(final CustomMU mu) {")
      linkMuList.zipWithIndex.foreach((field, idx) =>
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        if (idx == 0)
          writer.print(" " * 6 + s"if (mu.$fieldName().m() > mu.$fieldName().u()")
        else
          writer.print(" " * 10 + s"&& mu.$fieldName().m() > mu.$fieldName().u()")
        end if
        if (idx + 1 < linkMuList.length)
          writer.println()
        else
          writer.println(") {")
        end if
      )
      if (!linkMuList.isEmpty)
        writer.println(" " * 9 + "updatedLinkFields = new LinkFields(")
        linkMuList.zipWithIndex.foreach((field, idx) =>
          val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
          val comparison = field.linkMetaData.get.comparison
          val comparisonLevels = field.linkMetaData.get.comparisonLevels

          def extractComparisonList(levels: List[Double]): String = {
            levels.map(level => s""" ${level.toString}F""".stripMargin).mkString(",").trim
          }

          writer.print(" " * 12 + s"new LinkerProbabilistic.Field($comparison, ${
            if (comparisonLevels.length == 1) "List.of(" else "Arrays.asList("
          }${extractComparisonList(comparisonLevels)}), mu.$fieldName().m(), mu.$fieldName().u())")
          if (idx + 1 < linkMuList.length)
            writer.println(",")
          else
            writer.println(");")
          end if
        )
        writer.println(" " * 6 + "}")
      end if
      writer.println("   }")
      writer.println()

    end updateMU

  }

}
