package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_Shared/src/main/java/org/jembi/jempi/shared/models"
  private val customClassNameDemographicData = "CustomDemographicData"
  private val customClassNamePatientRecord = "PatientRecord"
  private val customClassNameGoldenRecord = "GoldenRecord"
  private val packageText = "org.jembi.jempi.shared.models"

  def generateDemographicData(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNameDemographicData + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import org.apache.commons.lang3.StringUtils;
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
         |   public static String getNames(final $customClassNameDemographicData demographicData) {
         |      var names = "";""".stripMargin)
    val names = fields.filter(f => f.fieldName.contains("name"))
    if (names.length > 0) {
      names.zipWithIndex.foreach {
        case (field, idx) =>
          val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
          writer.print(
            s"""      if (!StringUtils.isBlank(demographicData.$fieldName())) {
               |         names += ${(if (idx > 0) "\" \" + " else "")}demographicData.$fieldName();
               |      }
               |""".stripMargin);
      }
    } else {
      writer.println(
        """ "";""".stripMargin)
    }
    writer.println(
      s"""      return names;
         |   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generateDemographicData


//   def generatePatientRecord(fields: Array[Field]): Unit =
//     val classFile: String = classLocation + File.separator + customClassNamePatientRecord + ".java"
//     println("Creating " + classFile)
//     val file: File = new File(classFile)
//     val writer: PrintWriter = new PrintWriter(file)
//     writer.println(
//       s"""package $packageText;
//          |
//          |import com.fasterxml.jackson.annotation.JsonInclude;
//          |
//          |@JsonInclude(JsonInclude.Include.NON_NULL)
//          |public record $customClassNamePatientRecord(
//          |${" " * 6}String uid,
//          |${" " * 6}SourceId sourceId,
//          |${" " * 6}$customClassNameDemographicData demographicData) {
//          |}
//          |""".stripMargin)
//     writer.flush()
//     writer.close()
//   end generatePatientRecord

//   def generateGoldenRecord(fields: Array[Field]): Unit =
//     val classFile: String = classLocation + File.separator + customClassNameGoldenRecord + ".java"
//     println("Creating " + classFile)
//     val file: File = new File(classFile)
//     val writer: PrintWriter = new PrintWriter(file)
//     writer.println(
//       s"""package $packageText;
//          |
//          |import com.fasterxml.jackson.annotation.JsonInclude;
//          |
//          |import java.util.List;
//          |
//          |@JsonInclude(JsonInclude.Include.NON_NULL)
//          |public record $customClassNameGoldenRecord(
//          |${" " * 6}String uid,
//          |${" " * 6}List<SourceId> sourceId,
//          |${" " * 6}$customClassNameDemographicData demographicData) {
//          |
//          |${" " * 3}public $customClassNameGoldenRecord(final $customClassNamePatientRecord patient) {
//          |${" " * 6}this(null, List.of(patient.sourceId()), patient.demographicData());
//          |${" " * 3}}
//          |
//          |}""".stripMargin)

//     writer.flush()
//     writer.close()
//   end generateGoldenRecord

}
