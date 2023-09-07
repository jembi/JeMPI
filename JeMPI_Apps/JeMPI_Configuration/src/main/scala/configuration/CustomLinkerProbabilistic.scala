package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerProbabilistic {

  private val classLocation = "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker/backend"
  private val custom_className = "CustomLinkerProbabilistic"
  private val packageText = "org.jembi.jempi.linker.backend"

  def parseRules(config: Config): Any = {

    def generateFieldsRecord(writer: PrintWriter, recordName: String, demographicFields: Array[DemographicField]): Unit = {
      writer.println(s"   private record $recordName(")
      demographicFields.zipWithIndex.foreach((mu, idx) => {
        writer.print(s"""${" " * 9}LinkerProbabilistic.Field ${Utils.snakeCaseToCamelCase(mu.fieldName)}""")
        if (idx + 1 < demographicFields.length)
          writer.println(",")
        else
          writer.println(
            s""") {
               |   }""".stripMargin)
          writer.println()
        end if
      })
    }

    def generateCurrentFields(writer: PrintWriter, recordName: String, varName: String, linking: Boolean, demographicFields: Array[DemographicField]): Unit = {
      writer.print(s"""   static $recordName $varName =
                        |      new $recordName(
                        |         """.stripMargin)
      var margin = 0
      demographicFields.zipWithIndex.foreach((field, idx) => {
        if ((linking && field.linkMetaData.isDefined) ||
          (!linking && field.validateMetaData.isDefined)) {
          val comparison =   if (linking) field.linkMetaData.get.comparison else field.validateMetaData.get.comparison
          val comparisonLevels = if (linking) field.linkMetaData.get.comparisonLevels else field.validateMetaData.get.comparisonLevels
          val m: Double = if (linking) field.linkMetaData.get.m else field.validateMetaData.get.m
          val u: Double = if (linking) field.linkMetaData.get.u else field.validateMetaData.get.u

          def extractComparisonList(levels: List[Double]): String = {
            levels.map(level => s""" ${level.toString}F""".stripMargin).mkString(",").trim
          }

          writer.print(" " * margin + s"new LinkerProbabilistic.Field($comparison, ${if (comparisonLevels.length == 1) "List.of(" else "Arrays.asList("}${extractComparisonList(comparisonLevels)}), ${m}F, ${u}F)")
          if (idx + 1 < demographicFields.length)
            writer.println(",")
            margin = 9
          else
            writer.println(");")
            writer.println()
        }
      })

    }

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

    writer.println(s"""package $packageText;""")
    if (linkMuList.length == 0) {
      writer.println()
      writer.println(
        s"""
           |import org.jembi.jempi.shared.models.CustomDemographicData;
           import org.jembi.jempi.shared.models.CustomMU;
           |
           |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.EXACT_SIMILARITY;
           |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JACCARD_SIMILARITY;
           |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_SIMILARITY;
           |import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_WINKLER_SIMILARITY;
           |
           |final class $custom_className {
           |
           |  private $custom_className() {
           |  }
           |
           |  public static float linkProbabilisticScore(final CustomDemographicData goldenRecord,
           |                                             final CustomDemographicData interaction) {
           |    return 0.0F;
           |  }
           |
           |  public static void updateMU(final CustomMU mu) {
           |  }
           |
           |  public static void checkUpdatedMU() {
           |  }
           |
           |  static CustomMU getMU() {
           |    return new CustomMU(null);
           |  }
           |
           |}""".stripMargin)
    } else {
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
           |final class $custom_className {
           |
           |   static LinkFields updatedFields = null;
           |
           |   private $custom_className() {
           |   }
           |""".stripMargin)

      writer.println("   static CustomMU getMU() {")
      writer.println("      return new CustomMU(")
      linkMuList.zipWithIndex.foreach((mu, idx) => {
        writer.print(" " * 9 + s"LinkerProbabilistic.getProbability(currentLinkFields.${Utils.snakeCaseToCamelCase(mu.fieldName)})")
        if (idx + 1 < linkMuList.length)
          writer.println(",")
        else
          writer.println(
            """);
              |   }
              |""".stripMargin)
      })


      generateFieldsRecord(writer, "LinkFields", linkMuList)
      if (!validateMuList.isEmpty) {
        generateFieldsRecord(writer, "ValidateFields", validateMuList)
      }
      generateCurrentFields(writer, "LinkFields", "currentLinkFields", true, linkMuList)
      if (!validateMuList.isEmpty) {
        generateCurrentFields(writer, "ValidateFields", "currentValidateFields", false, validateMuList)
      }

      writer.println(
        """   static float linkProbabilisticScore(
          |         final CustomDemographicData goldenRecord,
          |         final CustomDemographicData interaction) {
          |      // min, max, score, missingPenalty
          |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
      linkMuList.zipWithIndex.foreach((field, _) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(" " * 6 + "LinkerProbabilistic.updateMetricsForStringField(metrics,")
        writer.println(" " * 54 + s"goldenRecord.$fieldName, interaction.$fieldName, currentLinkFields" +
          s".$fieldName);")
      })
      writer.println(
        s"""${" " * 6}return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
           |${" " * 3}}
           |""".stripMargin)

      writer.println(
        """   static float validateProbabilisticScore(
          |         final CustomDemographicData goldenRecord,
          |         final CustomDemographicData interaction) {
          |      // min, max, score, missingPenalty
          |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
      validateMuList.zipWithIndex.foreach((field, _) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(" " * 6 + "LinkerProbabilistic.updateMetricsForStringField(metrics,")
        writer.println(" " * 54 + s"goldenRecord.$fieldName, interaction.$fieldName, currentValidateFields" +
          s".$fieldName);")
      })
      writer.println(
        s"""${" " * 6}return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
           |${" " * 3}}
           |""".stripMargin)

      writer.println("   public static void updateMU(final CustomMU mu) {")
      linkMuList.zipWithIndex.foreach((field, idx) => {
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
      })
      writer.println(" " * 9 + "updatedFields = new LinkFields(")
      linkMuList.zipWithIndex.foreach((field, idx) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        val comparison = field.linkMetaData.get.comparison
        val comparisonLevels = field.linkMetaData.get.comparisonLevels

        def extractComparisonList(levels: List[Double]): String =
          levels.map(level => s""" ${level.toString}F""".stripMargin).mkString(",").trim
        end extractComparisonList

        writer.print(" " * 12 + s"new LinkerProbabilistic.Field($comparison, ${if (comparisonLevels.length == 1) "List.of(" else "Arrays.asList("}${extractComparisonList(comparisonLevels)}), mu.$fieldName().m(), mu.$fieldName().u())")
        if (idx + 1 < linkMuList.length)
          writer.println(",")
        else
          writer.println(");")
        end if

      })
      writer.println(" " * 6 + "}")
      writer.println("   }")
      writer.println()
      writer.println("}")
    }
    writer.flush()
    writer.close()
  }

}
