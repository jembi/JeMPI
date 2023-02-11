package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassNameDemographicData = "CustomDemographicData"
  private val customClassNamePatient = "CustomPatient"
  private val customClassNameGoldentRecord = "CustomGoldenRecord"
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
         |public record $customClassNameDemographicData(""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        writer.print(s"""${" " * (if (idx == 0) 0 else 36)}""")
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(typeString + " " + fieldName)
        writer.println(if (idx + 1 < fields.length) "," else ") {")
    }

    //    writer.println(
    //      s"""   public $customClassNameDoc() {
    //         |      this(null,
    //         |           null,""".stripMargin)
    //    fields.zipWithIndex.foreach {
    //      case (_, idx) =>
    //        writer.println(" " * 11 + (if (idx + 1 < fields.length) "null," else "null);"))
    //    }
    //    writer.println(
    //      s"""   }
    //         |
    //         |""".stripMargin)

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
         |import org.apache.commons.lang3.StringUtils;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNamePatient(String uid,
         |${" " * 28}SourceId sourceId,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        writer.print(" " * 28)
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(typeString + " " + fieldName)
        writer.println(if (idx + 1 < fields.length) "," else ") {")
    }

//    writer.println(
//      s"""   public $customClassNameDoc() {
//         |      this(null,
//         |           null,""".stripMargin)
//    fields.zipWithIndex.foreach {
//      case (_, idx) =>
//        writer.println(" " * 11 + (if (idx + 1 < fields.length) "null," else "null);"))
//    }
//    writer.println(
//      s"""   }
//         |
//         |""".stripMargin)

    writer.print(
      s"""   public static String getNames(final CustomPatient patient) {
         |      return """.stripMargin)
    val names = fields.filter(f => f.fieldName.contains("name"))
    println(names.length)
    if (names.length > 0) {
      names.zipWithIndex.foreach {
        case (field, idx) =>
          if (idx > 0) writer.print(" " * 14)
          val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
          writer.print(if (idx == 0) "(" else "")
          writer.print(s"""(StringUtils.isBlank(patient.$fieldName) ? "" : " " + patient.$fieldName)""")
          writer.println(if (idx + 1 < names.length) " + " else ").trim();")
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
    val classFile: String = classLocation + File.separator + customClassNameGoldentRecord + ".java"
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
         |public record $customClassNameGoldentRecord(String uid,
         |${" " * 33}List<SourceId> sourceId,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        writer.print(" " * 33)
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        val typeString = (if field.isList.isDefined && field.isList.get then "List<" else "") +
          field.fieldType
          + (if field.isList.isDefined && field.isList.get then ">" else "")
        writer.print(typeString + " " + fieldName)
        writer.println(if (idx + 1 < fields.length) "," else ") {")
    }

//    writer.println(
//      s"""   public $customClassNameMpi() {
//         |      this(null,
//         |           null,""".stripMargin)
//    fields.zipWithIndex.foreach {
//      case (_, idx) =>
//        writer.println(" " * 11 + (if (idx + 1 < fields.length) "null," else "null);"))
//    }
//    writer.println(
//      s"""   }""")

    writer.println(
      s"""
         |   public CustomGoldenRecord(final CustomPatient patient) {
         |      this(null,
         |           List.of(patient.sourceId()),""".stripMargin)

    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val fieldName = (if (field.isList.isDefined && field.isList.get) "List.of(" else "")
          + "patient." + Utils.snakeCaseToCamelCase(field.fieldName) + "()"
          + (if (field.isList.isDefined && field.isList.get) ")" else "")
        writer.println(s"""${" " * 11}$fieldName${if (idx + 1 < fields.length) "," else ");"}""")
    }
    writer.println(
      s"""   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generateGoldenRecord
}
