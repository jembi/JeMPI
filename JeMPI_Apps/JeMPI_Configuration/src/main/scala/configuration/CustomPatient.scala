package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_Shared/src/main/java/org/jembi/jempi/shared/models"
  private val customClassNameDemographicData = "CustomDemographicData"
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
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  end generateDemographicData
}
