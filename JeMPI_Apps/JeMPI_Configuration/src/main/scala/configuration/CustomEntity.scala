package org.jembi.jempi
package configuration

import java.io.{File, PrintWriter}

private object CustomEntity {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassNameDoc = "CustomEntity"
  private val customClassNameMpi = "CustomGoldenRecord"
  private val packageText = "org.jembi.jempi.shared.models"

  def generateEntity(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNameDoc + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameDoc(String uid,
         |${" " * 27}SourceId sourceId,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        writer.print(" " * 27)
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(typeString + " " + fieldName)
        writer.println(if (idx + 1 < fields.length) "," else ") {")
    }
    writer.println(
      s"""   public $customClassNameDoc() {
         |      this(null,
         |           null,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (_, idx) =>
        writer.println(" " * 11 + (if (idx + 1 < fields.length) "null," else "null);"))
    }
    writer.println(
      s"""   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generateEntity


  def generateGoldenRecord(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNameMpi + ".java"
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
         |public record $customClassNameMpi(String uid,
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
    writer.println(
      s"""   public $customClassNameMpi() {
         |      this(null,
         |           null,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (_, idx) =>
        writer.println(" " * 11 + (if (idx + 1 < fields.length) "null," else "null);"))
    }
    writer.println(
      s"""   }
         |
         |   public CustomGoldenRecord(final CustomEntity entity) {
         |      this(null,
         |           List.of(entity.sourceId()),""".stripMargin)

    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val fieldName = (if (field.isList.isDefined && field.isList.get) "List.of(" else "")
          + "entity." + Utils.snakeCaseToCamelCase(field.fieldName) + "()"
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
