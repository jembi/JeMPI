package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_LibShared/src/main/java/org/jembi/jempi/shared/models"
  private val packageText = "org.jembi.jempi.shared.models"
  private val customClassNameCustomDemographicData = "CustomDemographicData"
  private val customClassNameCustomUniqueGoldenRecordData = "CustomUniqueGoldenRecordData"
  private val customClassNameCustomUniqueInteractionData = "CustomUniqueInteractionData"

  private val classCustomDemographicDataFile: String = classLocation + File.separator + customClassNameCustomDemographicData + ".java"
  private val classCustomUniqueGoldenRecordDataFile: String = classLocation + File.separator + customClassNameCustomUniqueGoldenRecordData + ".java"
  private val classCustomUniqueInteractionDataFile: String = classLocation + File.separator + customClassNameCustomUniqueInteractionData + ".java"

  private val indent = 3

  private def generateDemographicData(config: Config): Unit =

    def cleanedFields(config: Config): String =
        config
          .commonFields
          .map(f =>
            s"""${" " * 39}this.${Utils.snakeCaseToCamelCase(f.fieldName)}.toLowerCase().replaceAll("\\\\W", ""),""")
          .mkString("\n")
          .trim
          .dropRight(1)
    end cleanedFields

    println("Creating " + classCustomDemographicDataFile)
    val file: File = new File(classCustomDemographicDataFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public class $customClassNameCustomDemographicData {
         |""".stripMargin)
    config.commonFields.zipWithIndex.foreach {
      case (field, idx) =>
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(s"""${" " * (indent * 1)}public final ${typeString} ${fieldName};""")
    }
    writer.println();
    for (field <- config.commonFields) {
      val typeString = field.fieldType
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.println(
        s"""${" " * (indent * 1)}public final ${typeString} get${fieldName.charAt(0).toUpper}${fieldName.substring(1)}() {
           |${" " * (indent * 2)}return ${fieldName};
           |${" " * (indent * 1)}}
           |""".stripMargin)
    }

    writer.println(s"""${" " * indent * 1}public $customClassNameCustomDemographicData() {""".stripMargin)
    writer.println(
      s"""${" " * indent * 2}this(${"null, " * (config.commonFields.length - 1)}null);
         |${" " * indent * 1}}
         |""".stripMargin)

    writer.println(
      s"""${" " * indent * 1}public $customClassNameCustomDemographicData(""".stripMargin)
    config.commonFields.zipWithIndex.foreach {
      case (field, idx) =>
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(
          s"""${" " * indent * 2}final $typeString $fieldName${if (idx < config.commonFields.length - 1) ',' else ") {"}""".stripMargin)
    }
    config.commonFields.zipWithIndex.foreach {
      case (field, idx) =>
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(
          s"""${" " * indent * 3}this.$fieldName = $fieldName;""".stripMargin)
    }
    writer.println(
      s"""${" " * indent * 1}}
         |
         |   public ${customClassNameCustomDemographicData} clean() {
         |      return new ${customClassNameCustomDemographicData}(${cleanedFields(config)});
         |   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()

  end generateDemographicData

  private def generateUniqueGoldenRecordData(config: Config): Unit =

    def fields(config: Config): String =
      if (config.uniqueGoldenRecordFields.isEmpty) "" else
        config
          .uniqueGoldenRecordFields
          .get
          .map(f =>
            s"""${" " * 43}${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
          .mkString("\n")
          .trim
          .dropRight(1)
    end fields

    println("Creating " + classCustomUniqueGoldenRecordDataFile)
    val file: File = new File(classCustomUniqueGoldenRecordDataFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameCustomUniqueGoldenRecordData(${fields(config)}) {
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()
  end generateUniqueGoldenRecordData

  private def generateUniqueInteractionData(config: Config): Unit =

    def fields(config: Config): String =
      if (config.uniqueInteractionFields.isEmpty) "" else
        config
          .uniqueInteractionFields
          .get
          .map(f =>
            s"""${" " * 42}${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
          .mkString("\n")
          .trim
          .dropRight(1)
    end fields

    println("Creating " + classCustomUniqueInteractionDataFile)
    val file: File = new File(classCustomUniqueInteractionDataFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameCustomUniqueInteractionData(${fields(config)}) {
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()
  end generateUniqueInteractionData

  def generate(config: Config): Unit =
    generateDemographicData(config)
    generateUniqueGoldenRecordData(config)
    generateUniqueInteractionData(config)
  end generate

}
