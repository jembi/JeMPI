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
         |import org.jembi.jempi.shared.models.CustomMU;
         |
         |import java.util.Collections;
         |import java.util.List;
         |
         |import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;
         |
         |final class $custom_className {
         |
         |   static final boolean PROBABILISTIC_DO_LINKING = ${
        if (config.demographicFields.exists(x => x.linkMetaData.isDefined)) "true"
        else "false"
      };
         |   static final boolean PROBABILISTIC_DO_VALIDATING = ${if (config.demographicFields.exists(x => x.validateMetaData.isDefined)) "true" else "false"};
         |   static final boolean PROBABILISTIC_DO_MATCHING = ${if (config.demographicFields.exists(x => x.matchMetaData.isDefined)) "true" else "false"};
         |
         |
         |   private $custom_className() {
         |   }
         |""".stripMargin)

    //         |${if (linkMuList.isEmpty) "" else "   static LinkFields updatedLinkFields = null;"}
    //         |${if (validateMuList.isEmpty) "" else "   static ValidateFields updatedValidateFields = null;"}
    //         |${if (matchNotificationMuList.isEmpty) "" else "   static MatchNotificationFields updatedMatchNotificationFields = null;"}


    // generateGetMU()
//    if (!linkMuList.isEmpty) {
//      generateFieldsRecord("LinkFields", linkMuList)
//    }
//    if (!validateMuList.isEmpty) {
//      generateFieldsRecord("ValidateFields", validateMuList)
//    }
//    if (!matchNotificationMuList.isEmpty) {
//      generateFieldsRecord("MatchNotificationFields", matchNotificationMuList)
//    }
//
//    if (!linkMuList.isEmpty) {
//      generateCurrentFields("LinkFields", "currentLinkFields", true, linkMuList)
//    }
//    if (!validateMuList.isEmpty) {
//      generateCurrentFields("ValidateFields", "currentValidateFields", false, validateMuList)
//    }
//    if (!matchNotificationMuList.isEmpty) {
//      generateCurrentFields("MatchNotificationFields", "currentMatchNotificationFields", false, matchNotificationMuList)
//    }

//    linkProbabilisticScore()
//    validateProbabilisticScore()
//    matchNotificationProbabilisticScore()
    toLinkProbabilisticFieldList()
    toValidateProbabilisticFieldList()
    toMatchProbabilisticFieldList()
//    checkUpdatedMU()

    writer.println("}")
    writer.flush()
    writer.close()


    def generateGetMU(): Unit =
      if (!linkMuList.isEmpty)
        writer.println("   static CustomMU getMU() {")
        writer.println("      return new CustomMU(")
        linkMuList.zipWithIndex.foreach((mu, idx) =>
                                          writer.print(" " * 9 + s"LinkerProbabilistic.getProbability(currentLinkFields.${
                                            Utils.snakeCaseToCamelCase(mu.fieldName)
                                          })")
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

                                                 def extractComparisonList(levels: List[Double]): String = levels.map(level =>
                                                                                                                        s""" ${
                                                                                                                          level.toString
                                                                                                                        }F""".stripMargin).mkString(",").trim

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
          |         final DemographicData goldenRecord,
          |         final DemographicData interaction) {""".stripMargin)
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
                                        writer.println(" " * 54 + s"goldenRecord.fields.get(${field.fieldName.toUpperCase}).value(), interaction.fields.get(${field.fieldName.toUpperCase}).value(), currentLinkFields" +
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
        """   static float validateProbabilisticScore(
          |         final DemographicData goldenRecord,
          |         final DemographicData interaction) {""".stripMargin)

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
          writer.println(" " * 54 + s"goldenRecord.fields.get(${field.fieldName.toUpperCase}).value(), interaction.fields.get(${field.fieldName.toUpperCase}).value(), currentValidateFields" +
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
          |         final DemographicData goldenRecord,
          |         final DemographicData interaction) {""".stripMargin)
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
                                          writer.println(" " * 54 + s"goldenRecord.fields.get(${field.fieldName.toUpperCase}).value(), interaction.fields.get(${field.fieldName.toUpperCase}).value(), currentMatchNotificationFields" +
                                                         s".$fieldName);")
                                        )
        writer.print(
          s"""${" " * 6}return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
             |""".stripMargin)
      end if
      writer.println("   }")
    end matchNotificationProbabilisticScore

    def toLinkProbabilisticFieldList(): Unit =
      writer.println("   static List<LinkerProbabilistic.ProbabilisticField> toLinkProbabilisticFieldList(final CustomMU.CustomLinkMU mu) {")
      if (linkMuList.length > 0)
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
           writer.println(" " * 9 + "return List.of(")
           linkMuList.zipWithIndex.foreach((field, idx) =>
                                             val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
                                             writer.print(
                                               s"""${" " * 12}new LinkerProbabilistic
                                                  |${" " * 15}.ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(LINKER_CONFIG.probabilisticLinkFields.get($idx)
                                                  |${" " * 15}                                                                                                   .similarityScore()),
                                                  |${" " * 15}                     LINKER_CONFIG.probabilisticLinkFields.get($idx).comparisonLevels(),
                                                  |${" " * 15}                     mu.$fieldName().m(),
                                                  |${" " * 15}                     mu.$fieldName().u())""".stripMargin)
                                             if (idx + 1 < linkMuList.length)
                                               writer.println(",")
                                             else
                                               writer.println(");")
                                             end if
                                           )
           writer.println(
             s"""      } else {
                |         return Collections.emptyList();
                |      }
                |    }
                |""".stripMargin)
      else
        writer.println(
          s"""      return Collections.emptyList();
             |   }
             |""".stripMargin)
      end if
    end toLinkProbabilisticFieldList

    def toValidateProbabilisticFieldList(): Unit =
      writer.println("   static List<LinkerProbabilistic.ProbabilisticField> toValidateProbabilisticFieldList(final CustomMU.CustomValidateMU mu) {")
      if (validateMuList.length > 0)
        validateMuList.zipWithIndex.foreach((field, idx) =>
                                           val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
                                           if (idx == 0)
                                             writer.print(" " * 6 + s"if (mu.$fieldName().m() > mu.$fieldName().u()")
                                           else
                                             writer.print(" " * 10 + s"&& mu.$fieldName().m() > mu.$fieldName().u()")
                                           end if
                                           if (idx + 1 < validateMuList.length)
                                             writer.println()
                                           else
                                             writer.println(") {")
                                           end if
                                         )
         writer.println(" " * 9 + "return List.of(")
         validateMuList.zipWithIndex.foreach((field, idx) =>
                                           val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
                                           writer.print(
                                             s"""${" " * 12}new LinkerProbabilistic
                                                |${" " * 15}.ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(LINKER_CONFIG.probabilisticValidateFields.get($idx)
                                                |${" " * 15}                                                                                                       .similarityScore()),
                                                |${" " * 15}                     LINKER_CONFIG.probabilisticValidateFields.get($idx).comparisonLevels(),
                                                |${" " * 15}                     mu.$fieldName().m(),
                                                |${" " * 15}                     mu.$fieldName().u())""".stripMargin)
                                           if (idx + 1 < validateMuList.length)
                                             writer.println(",")
                                           else
                                             writer.println(");")
                                           end if
                                         )

         writer.println(s"""
         |     } else {
         |        return Collections.emptyList();
         |     }
         |   }
         |""".stripMargin)
      else
        writer.println(
          s"""      return Collections.emptyList();
            |   }
          |""".stripMargin)
      end if
    end toValidateProbabilisticFieldList

    def toMatchProbabilisticFieldList(): Unit =
       writer.println("   static List<LinkerProbabilistic.ProbabilisticField> toMatchProbabilisticFieldList(final CustomMU.CustomMatchMU mu) {")
       if (matchNotificationMuList.length > 0)
          matchNotificationMuList.zipWithIndex.foreach((field, idx) =>
                                                val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
                                                if (idx == 0)
                                                  writer.print(" " * 6 + s"if (mu.$fieldName().m() > mu.$fieldName().u()")
                                                else
                                                  writer.print(" " * 10 + s"&& mu.$fieldName().m() > mu.$fieldName().u()")
                                                end if
                                                if (idx + 1 < matchNotificationMuList.length)
                                                  writer.println()
                                                else
                                                  writer.println(") {")
                                                end if
                                              )
          writer.println(" " * 9 + "return List.of(")
          matchNotificationMuList.zipWithIndex.foreach((field, idx) =>
                                                val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
                                                writer.print(
                                                  s"""${" " * 12}new LinkerProbabilistic
                                                     |${" " * 15}.ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(LINKER_CONFIG.probabilisticMatchNotificationFields.get($idx)
                                                     |${" " * 15}                                                                                                                .similarityScore()),
                                                     |${" " * 15}                     LINKER_CONFIG.probabilisticMatchNotificationFields.get($idx).comparisonLevels(),
                                                     |${" " * 15}                     mu.$fieldName().m(),
                                                     |${" " * 15}                     mu.$fieldName().u())""".stripMargin)
                                                if (idx + 1 < matchNotificationMuList.length)
                                                  writer.println(",")
                                                else
                                                  writer.println(");")
                                                end if
                                              )
          writer.println(s"""
               |     } else {
               |        return Collections.emptyList();
               |     }
               |   }
               |""".stripMargin)
       else
         writer.println(
           s"""      return Collections.emptyList();
              |   }
              |""".stripMargin)
       end if
    end toMatchProbabilisticFieldList

    def checkUpdatedMU(): Unit =
    
        def generateCode(): String =
          val s1 = (if (linkMuList.length > 0)
            s"""      if (updatedLinkFields != null) {
                |         LOGGER.info("Using updated Link MU values: {}", updatedLinkFields);
                |         CustomLinkerProbabilistic.currentLinkFields = updatedLinkFields;
                |         updatedLinkFields = null;
                |     }
                |""".stripMargin else "")
    
          val s2 = (if (validateMuList.length > 0)
            s"""     if (updatedValidateFields != null) {
               |         LOGGER.info("Using updated Validate MU values: {}", updatedValidateFields);
               |         CustomLinkerProbabilistic.currentValidateFields = updatedValidateFields;
               |         updatedValidateFields = null;
               |     }
               |""".stripMargin else "")
    
          val s3 = (if (matchNotificationMuList.length > 0)
            s"""     if (updatedMatchNotificationFields != null) {
               |         LOGGER.info("Using updated MatchNotification MU values: {}", updatedMatchNotificationFields);
               |         CustomLinkerProbabilistic.currentMatchNotificationFields = updatedMatchNotificationFields;
               |         updatedMatchNotificationFields = null;
               |     }""".stripMargin else "")
    
          s1 + s2 + s3
        end generateCode
    
        writer.println(
          s"""   public static void checkUpdatedLinkMU() {
             |${if (linkMuList.length > 0) generateCode() else ""}
             |   }
             |""".stripMargin)
      end checkUpdatedMU
    
    }

}
