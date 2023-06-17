package configuration

import java.io.{File, PrintWriter}

private object CustomAsyncHelper {

  private val classLocation = "../JeMPI_AsyncReceiver/src/main/java/org/jembi/jempi/async_receiver"
  private val customClassName = "CustomAsyncHelper"
  private val packageText = "org.jembi.jempi.async_receiver"

  private def columnIndices(config: Config): String =

    def additionalNodeFields(additionalNode: AdditionalNode): String =
      additionalNode.fields.map(f => s"""${" " * 3}private static final int ${additionalNode.nodeName.toUpperCase}_${f.fieldName.toUpperCase}_COL_NUM = ${f.csvCol.get}""").mkString(
        s""";
           |""".stripMargin).stripTrailing()
    end additionalNodeFields

    (if (config.uniqueInteractionFields.isEmpty) "" else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          s"""${" " * 3}private static final int ${f.fieldName.toUpperCase}_COL_NUM = ${f.csvCol.get};""")
        .mkString("\n") + "\n")
      +
      (if (config.additionalNodes.isEmpty)
        ""
      else
        (config.additionalNodes.get.map(x => s"""${additionalNodeFields(x)};""").mkString("\n"))) + "\n"
      +
      config
        .demographicFields
        .map(f =>
          s"""${" " * 3}private static final int ${f.fieldName.toUpperCase}_COL_NUM = ${f.csvCol.get};""")
        .mkString("\n")
  end columnIndices

  private def demographicFields(config: Config): String =
    config
      .demographicFields
      .map(f =>
        s"""${" " * 9}csvRecord.get(${f.fieldName.toUpperCase}_COL_NUM),""")
      .mkString("\n")
      .dropRight(1)
  end demographicFields

  private def customUniqueInteractionArguments(config: Config): String =
    if (config.uniqueInteractionFields.isEmpty) "" else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          if (f.fieldName.toUpperCase.equals("AUX_ID")) {
            s"""${" " * 45}Main.parseRecordNumber(csvRecord.get(${f.fieldName.toUpperCase}_COL_NUM))"""
          } else {
            s"""${" " * 45}csvRecord.get(${f.fieldName.toUpperCase}_COL_NUM)"""
          })
        .mkString(
          """,
            |""".stripMargin)
        .trim
  end customUniqueInteractionArguments

  def customNodeConstructor(additionalNode: AdditionalNode): String =

    def arguments(fields: Array[AdditionalNodeField]): String =
      fields.map(f => s"""         csvRecord.get(${additionalNode.nodeName.toUpperCase}_${f.fieldName.toUpperCase()}_COL_NUM)""").mkString(",\n")
    end arguments

    s"""   static Custom${additionalNode.nodeName} custom${additionalNode.nodeName}(final CSVRecord csvRecord) {
       |      return new Custom${additionalNode.nodeName}(
       |         null,
       |${arguments(additionalNode.fields)});
       |   }
       |""".stripMargin
  end customNodeConstructor

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    val margin = 33
    writer.println(
      s"""package $packageText;
         |
         |import org.apache.commons.csv.CSVRecord;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.CustomSourceId;
         |import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
         |
         |final class $customClassName {
         |
         |${columnIndices(config)}
         |
         |   private ${customClassName}() {
         |   }
         |
         |   static CustomUniqueInteractionData customUniqueInteractionData(final CSVRecord csvRecord) {
         |      return new CustomUniqueInteractionData(${customUniqueInteractionArguments(config)});
         |   }
         |
         |   static CustomDemographicData customDemographicData(final CSVRecord csvRecord) {
         |      return new CustomDemographicData(
         |${demographicFields(config)});
         |   }
         |
         |${if (config.additionalNodes.isEmpty) "" else config.additionalNodes.get.map(x => customNodeConstructor(x)).mkString}
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  end generate

}
