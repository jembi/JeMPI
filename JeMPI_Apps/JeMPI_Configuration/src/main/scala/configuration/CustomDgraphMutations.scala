package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphMutations {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val custom_className = "CustomDgraphMutations"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def interactionFields(config: Config): String =
    (if (config.uniqueInteractionFields.isEmpty) "" else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          s"""${" " * 27}_:%s  <Interaction.${f.fieldName}>${" " * (25 - f.fieldName.length)}%s${" " * 10}.""")
        .mkString("\n") + "\n")
      +
      config
        .commonFields
        .map(f =>
          s"""${" " * 27}_:%s  <Interaction.${f.fieldName}>${" " * (25 - f.fieldName.length)}%s${" " * 10}.""")
        .mkString("\n")
  end interactionFields

  private def interactionArguments(config: Config): String =

    def mapUniqueField(f: UniqueField): String =
      s"""AppUtils.quotedValue(uniqueInteractionData.${Utils.snakeCaseToCamelCase(f.fieldName)}())"""
    end mapUniqueField

    def mapCommonField(f: CommonField): String =
      s"""AppUtils.quotedValue(demographicData.${Utils.snakeCaseToCamelCase(f.fieldName)})"""
    end mapCommonField

    (if (config.uniqueInteractionFields.isEmpty) "" else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          s"""${" " * 27}uuid, ${mapUniqueField(f)},""")
        .mkString("\n") + "\n")
      +
      config
        .commonFields
        .map(f =>
          s"""${" " * 27}uuid, ${mapCommonField(f)},""")
        .mkString("\n")
  end interactionArguments

  private def castAs(t: String): String =
    t match
      case "String" => ""
      case "Bool" => "^^<xs:boolean>"
  end castAs

  private def goldenRecordFields(config: Config): String =
    (if (config.uniqueGoldenRecordFields.isEmpty) "" else
      config
        .uniqueGoldenRecordFields
        .get
        .map(f =>
          val c = castAs(f.fieldType)
          s"""${" " * 27}_:%s  <GoldenRecord.${f.fieldName}>${" " * (30 - f.fieldName.length)}%s${c}${" " * (20 - c.length)}."""
        )
        .mkString("\n") + "\n")
      +
      config
        .commonFields
        .map(f =>
          val c = castAs(f.fieldType)
          s"""${" " * 27}_:%s  <GoldenRecord.${f.fieldName}>${" " * (30 - f.fieldName.length)}%s${c}${" " * (20 - c.length)}.""")
        .mkString("\n")
  end goldenRecordFields

  private def goldenRecordArguments(config: Config): String =

    def checkToString(v: String): String =
      v match
        case "Bool" => ".toString()"
        case _ => ""
    end checkToString

    def mapUniqueField(f: UniqueField): String =
      s"""AppUtils.quotedValue(uniqueGoldenRecordData.${Utils.snakeCaseToCamelCase(f.fieldName)}()${checkToString(f.fieldType)})"""
    end mapUniqueField

    def mapDemographicField(f: CommonField): String =
        s"""AppUtils.quotedValue(demographicData.${Utils.snakeCaseToCamelCase(f.fieldName)})${checkToString(f.fieldType)}"""
    end mapDemographicField

    (if (config.uniqueGoldenRecordFields.isEmpty) "" else
      config
        .uniqueGoldenRecordFields
        .get
        .map(f =>
          s"""${" " * 27}uuid, ${mapUniqueField(f)},""")
        .mkString("\n") + "\n")
      +
      config
        .commonFields
        .map(f =>
          s"""${" " * 27}uuid, ${mapDemographicField(f)},""")
        .mkString("\n")
  end goldenRecordArguments

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
         |import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.utils.AppUtils;
         |
         |import java.util.UUID;
         |
         |final class $custom_className {
         |
         |   private $custom_className() {
         |   }
         |
         |   static String createInteractionTriple(
         |         final CustomUniqueInteractionData uniqueInteractionData,
         |         final CustomDemographicData demographicData,
         |         final String sourceUID) {
         |      final String uuid = UUID.randomUUID().toString();
         |      return String.format(\"\"\"
         |                           _:%s  <Interaction.source_id>${" " * 16}<%s>${" " * 8}.
         |${interactionFields(config)}
         |${" " * 27}_:%s  <dgraph.type>                     \"Interaction\"    .
         |${" " * 27}\"\"\",
         |${" " * 27}uuid, sourceUID,
         |${interactionArguments(config)}
         |${" " * 27}uuid);
         |   }
         |
         |   static String createLinkedGoldenRecordTriple(
         |         final CustomUniqueGoldenRecordData uniqueGoldenRecordData,
         |         final CustomDemographicData demographicData,
         |         final String interactionUID,
         |         final String sourceUID,
         |         final float score) {
         |      final String uuid = UUID.randomUUID().toString();
         |      return String.format(\"\"\"
         |                           _:%s  <GoldenRecord.source_id>                     <%s>                  .
         |${goldenRecordFields(config)}
         |${" " * 27}_:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
         |${" " * 27}_:%s  <dgraph.type>                                "GoldenRecord"        .
         |${" " * 27}\"\"\",
         |${" " * 27}uuid, sourceUID,
         |${goldenRecordArguments(config)}
         |${" " * 27}uuid, interactionUID, score,
         |${" " * 27}uuid);
         |${" " * 3}}
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
