package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassNameDemographicData = "CustomDemographicData"
  private val customClassNamePatient = "CustomPatient"
  private val customClassNameGoldenRecord = "CustomGoldenRecord"
  private val packageText = "org.jembi.jempi.shared.models"

  def generateDemographicData(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNameDemographicData + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameDemographicData(
         |""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        writer.print(s"""${" " * 6}""")
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(typeString + " " + fieldName)
        writer.println(if (idx + 1 < fields.length) "," else ") {")
    }
    writer.println(
      s"""
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generateDemographicData


  def generatePatient(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNamePatient + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import org.apache.commons.lang3.StringUtils;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNamePatient(
         |${" " * 6}String uid,
         |${" " * 6}SourceId sourceId,
         |${" " * 6}CustomDemographicData demographicData) {
         |""".stripMargin)

    writer.print(
      s"""   public static String getNames(final CustomPatient patient) {
         |      return """.stripMargin)
    val names = fields.filter(f => f.fieldName.contains("name"))
    if (names.length > 0) {
      names.zipWithIndex.foreach {
        case (field, idx) =>
          if (idx > 0) writer.print(" " * 14)
          val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
          writer.print(if (idx == 0) "(" else "")
          writer.print(
            s"""(StringUtils.isBlank(patient.demographicData.$fieldName())
               |${" " * 21}? ""
               |${" " * 21}: " " + patient.demographicData.$fieldName())""".stripMargin)
          writer.println(if (idx + 1 < names.length) " +" else ").trim();")
      }
    } else {
      writer.println(
        """ "";""".stripMargin)
    }
    writer.println(
      s"""   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generatePatient

  def generateGoldenRecord(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNameGoldenRecord + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameGoldenRecord(
         |${" " * 6}String uid,
         |${" " * 6}List<SourceId> sourceId,
         |${" " * 6}CustomDemographicData demographicData) {
         |
         |${" " * 3}public CustomGoldenRecord(final CustomPatient patient) {
         |${" " * 6}this(null, List.of(patient.sourceId()), patient.demographicData());
         |${" " * 3}}
         |
         |}""".stripMargin)

    writer.flush()
    writer.close()
  end generateGoldenRecord
}
