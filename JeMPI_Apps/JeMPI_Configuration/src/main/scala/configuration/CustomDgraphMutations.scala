package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphMutations {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val custom_className = "CustomDgraphMutations"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(config: Config): Unit =
    val classFile: String =
      classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(s"""package $packageText;
         |
         |import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
         |import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.utils.AppUtils;
         |
         |import java.util.Locale;
         |import java.util.UUID;
         |
         |import static org.jembi.jempi.shared.models.CustomDemographicData.*;
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
         |      return String.format(Locale.ROOT,
         |                           \"\"\"
         |                           _:%s  <Interaction.source_id>${" " * 21}<%s>${" " * 18}.
         |${interactionFields()}
         |${" " * 27}_:%s  <dgraph.type>                               \"Interaction\"         .
         |${" " * 27}\"\"\",
         |${" " * 27}uuid, sourceUID,
         |${interactionArguments()}
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
         |      return String.format(Locale.ROOT,
         |                           \"\"\"
         |                           _:%s  <GoldenRecord.source_id>                     <%s>                  .
         |${goldenRecordFields()}
         |${" " * 27}_:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
         |${" " * 27}_:%s  <dgraph.type>                                "GoldenRecord"        .
         |${" " * 27}\"\"\",
         |${" " * 27}uuid, sourceUID,
         |${goldenRecordArguments()}
         |${" " * 27}uuid, interactionUID, score,
         |${" " * 27}uuid);
         |${" " * 3}}
         |}""".stripMargin)
    writer.flush()
    writer.close()

    def checkToString(v: String): String =
      v match
        case "Bool"     => ".toString()"
        case "DateTime" => ".toString()"
        case _          => ""
    end checkToString

    def castAs(t: String): String =
      t match
        case "String"   => ""
        case "Bool"     => "^^<xs:boolean>"
        case "DateTime" => "^^<xs:dateTime>"
    end castAs

    def interactionFields(): String =

      def mapField(fieldName: String, fieldType: String): String =
        val c = castAs(fieldType)
        s"""${" " * 27}_:%s  <Interaction.$fieldName>${" " * (30 - fieldName.length)}%s$c${" " * (20 - c.length)}."""
      end mapField

      val f1 =
        if (config.uniqueInteractionFields.isEmpty) ""
        else
          config.uniqueInteractionFields.get
            .map(f => mapField(f.fieldName, f.fieldType))
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 = config.demographicFields
        .map(f => mapField(f.fieldName, f.fieldType))
        .mkString(sys.props("line.separator"))

      f1 + f2
    end interactionFields

    def interactionArguments(): String =

      def mapUniqueField(f: UniqueField): String =
        s"""AppUtils.quotedValue(uniqueInteractionData.${Utils
            .snakeCaseToCamelCase(f.fieldName)}()${checkToString(
            f.fieldType
          )})"""
      end mapUniqueField

      def mapCommonField(f: DemographicField): String =
        s"""AppUtils.quotedValue(demographicData.fields.get(${f.fieldName.toUpperCase}).value())${checkToString(
            f.fieldType
          )}"""
      end mapCommonField

      val f1 =
        if (config.uniqueInteractionFields.isEmpty) ""
        else
          config.uniqueInteractionFields.get
            .map(f => s"""${" " * 27}uuid, ${mapUniqueField(f)},""")
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 = config.demographicFields
        .map(f => s"""${" " * 27}uuid, ${mapCommonField(f)},""")
        .mkString(sys.props("line.separator"))

      f1 + f2

    end interactionArguments

    def goldenRecordFields(): String =

      def mapField(fieldName: String, fieldType: String): String =
        val c = castAs(fieldType)
        s"""${" " * 27}_:%s  <GoldenRecord.${fieldName}>${" " * (30 - fieldName.length)}%s$c${" " * (20 - c.length)}."""
      end mapField

      val f1 =
        if (config.uniqueGoldenRecordFields.isEmpty) ""
        else
          config.uniqueGoldenRecordFields.get
            .map(f => mapField(f.fieldName, f.fieldType))
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 = config.demographicFields
        .map(f => mapField(f.fieldName, f.fieldType))
        .mkString(sys.props("line.separator"))

      f1 + f2

    end goldenRecordFields

    def goldenRecordArguments(): String =

      def mapUniqueField(f: UniqueField): String =
        s"""AppUtils.quotedValue(uniqueGoldenRecordData.${Utils
            .snakeCaseToCamelCase(f.fieldName)}()${checkToString(
            f.fieldType
          )})"""
      end mapUniqueField

      def mapDemographicField(f: DemographicField): String =
        s"""AppUtils.quotedValue(demographicData.fields.get(${f.fieldName.toUpperCase}).value())${checkToString(
            f.fieldType
          )}"""
      end mapDemographicField

      val f1 =
        if (config.uniqueGoldenRecordFields.isEmpty) ""
        else
          config.uniqueGoldenRecordFields.get
            .map(f => s"""${" " * 27}uuid, ${mapUniqueField(f)},""")
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 = config.demographicFields
        .map(f => s"""${" " * 27}uuid, ${mapDemographicField(f)},""")
        .mkString(sys.props("line.separator"))

      f1 + f2

    end goldenRecordArguments

  end generate

}
