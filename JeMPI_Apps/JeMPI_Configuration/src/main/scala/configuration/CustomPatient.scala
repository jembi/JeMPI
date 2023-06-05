package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_LibShared/src/main/java/org/jembi/jempi/shared/models"
  private val packageText = "org.jembi.jempi.shared.models"
  private val customClassNameDemographicData = "CustomDemographicData"
  private val classFile: String = classLocation + File.separator + customClassNameDemographicData + ".java"
  private val indent = 3

  def generateDemographicData(fields: Array[CommonField]): Unit =

    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public class $customClassNameDemographicData {
         |""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(s"""${" " * (indent * 1)}public final ${typeString} ${fieldName};""")
    }
    writer.println();
    for (field <- fields) {
      val typeString = field.fieldType
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.println(
        s"""${" " * (indent * 1)}public final ${typeString} get${fieldName.charAt(0).toUpper}${fieldName.substring(1)}() {
           |${" " * (indent * 2)}return ${fieldName};
           |${" " * (indent * 1)}}
           |""".stripMargin)
    }

    writer.println(s"""${" " * indent * 1}public $customClassNameDemographicData() {""".stripMargin)
    writer.println(
      s"""${" " * indent * 2}this(${"null, " * (fields.length - 1)}null);
         |${" " * indent * 1}}
         |""".stripMargin)

    writer.println(
      s"""${" " * indent * 1}public $customClassNameDemographicData(""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(
          s"""${" " * indent * 2}final $typeString $fieldName${if (idx < fields.length - 1) ',' else ") {"}""".stripMargin)
    }
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(
          s"""${" " * indent * 3}this.$fieldName = $fieldName;""".stripMargin)
    }
    writer.println(
      s"""${" " * indent * 1}}
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()

  end generateDemographicData

}
