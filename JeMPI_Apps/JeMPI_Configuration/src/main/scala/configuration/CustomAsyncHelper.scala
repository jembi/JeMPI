package configuration

import java.io.{File, PrintWriter}

private object CustomAsyncHelper {

  private val classLocation = "../JeMPI_AsyncReceiver/src/main/java/org/jembi/jempi/async_receiver"
  private val customClassName = "CustomAsyncHelper"
  private val packageText = "org.jembi.jempi.async_receiver"

  private def columnIndices(config: Config): String =
    (if (config.uniqueInteractionFields.isEmpty) "" else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          s"""${" " * 3}private static final int ${f.fieldName.toUpperCase}_COL_NUM = ${f.csvCol.get};""")
        .mkString("\n") + "\n")
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
         |      return new CustomUniqueInteractionData(Main.parseRecordNumber(csvRecord.get(AUX_ID_COL_NUM)));
         |   }
         |
         |   static CustomDemographicData customDemographicData(final CSVRecord csvRecord) {
         |      return new CustomDemographicData(
         |${demographicFields(config)});
         |   }
         |
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  end generate

}
