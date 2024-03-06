package configuration

import java.io.{File, PrintWriter}

private object CustomMU {

  private val classLocation =
    "../JeMPI_LibShared/src/main/java/org/jembi/jempi/shared/models"
  private val customClassName = "CustomMU"
  private val packageSharedModels = "org.jembi.jempi.shared.models"

  def generate(fields: Array[DemographicField]): Unit =
    val classFile: String =
      classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val linkFilteredFields = fields.filter(f => f.linkMetaData.isDefined)
    val validateFilteredFields =
      fields.filter(f => f.validateMetaData.isDefined)
    val matchFilteredFields = fields.filter(f => f.matchMetaData.isDefined)

    val sendToEM =
      (linkFilteredFields.length + validateFilteredFields.length + matchFilteredFields.length) > 0

    writer.print(s"""package $packageSharedModels;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassName(String tag,
         |                       CustomLinkMU customLinkMU,
         |                       CustomValidateMU customValidateMU,
         |                       CustomMatchMU customMatchMU) {
         |
         |   public static final Boolean SEND_INTERACTIONS_TO_EM = ${
                     if (sendToEM) "true" else "false"
                   };
         |
         |   public record Probability(float m, float u) {
         |   }
         |
         |   public record CustomLinkMU(""".stripMargin)
    val linkMargin = 30
    if (linkFilteredFields.length == 0)
      writer.println(s"""Probability dummy) {}""".stripMargin)
    else
      linkFilteredFields.zipWithIndex.foreach { case (f, i) =>
        val parameterName = Utils.snakeCaseToCamelCase(f.fieldName)
        writer.print(" " * (if (i == 0) 0 else linkMargin))
        writer.print(s"Probability $parameterName")
        if (i + 1 < linkFilteredFields.length) writer.println(",")
        else
          writer.println(s""") {
               |   }""".stripMargin)
        end if
      }
    end if

    writer.print(s"""
         |   public record CustomValidateMU(""".stripMargin)
    val validateMargin = 34
    if (validateFilteredFields.length == 0)
      writer.println(s"""Probability dummy) {
           |   }""".stripMargin)
    else
      validateFilteredFields.zipWithIndex.foreach { case (f, i) =>
        val parameterName = Utils.snakeCaseToCamelCase(f.fieldName)
        writer.print(" " * (if (i == 0) 0 else validateMargin))
        writer.print(s"Probability $parameterName")
        if (i + 1 < validateFilteredFields.length) writer.println(",")
        else
          writer.println(s""") {
               |   }""".stripMargin)
        end if
      }
    end if

    writer.print(s"""
         |   public record CustomMatchMU(""".stripMargin)
    val matchMargin = 31
    if (matchFilteredFields.length == 0)
      writer.println(s"""Probability dummy) {
           |   }""".stripMargin)
    else
      matchFilteredFields.zipWithIndex.foreach { case (f, i) =>
        val parameterName = Utils.snakeCaseToCamelCase(f.fieldName)
        writer.print(" " * (if (i == 0) 0 else matchMargin))
        writer.print(s"Probability $parameterName")
        if (i + 1 < matchFilteredFields.length) writer.println(",")
        else
          writer.println(s""") {
               |   }""".stripMargin)
        end if
      }
    end if

    writer.println()
    writer.println(s"""}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
