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
    writer.print(s"""package $packageSharedModels;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassName(String tag,
         |""".stripMargin)
    val margin = 23
    val filteredFields = fields.filter(f => f.linkMetaData.isDefined)
    if (filteredFields.length == 0)
      writer.println(s"""              Probability dummy) {
           |
           |   public static final Boolean SEND_INTERACTIONS_TO_EM = false;
           |""".stripMargin)
    else
      filteredFields.zipWithIndex.foreach { case (f, i) =>
        val parameterName = Utils.snakeCaseToCamelCase(f.fieldName)
        writer.print(" " * margin)
        writer.print(s"Probability $parameterName")
        if (i + 1 < filteredFields.length) writer.println(",")
        else
          writer.println(") {")
          writer.print(
            s"""
               |   public static final Boolean SEND_INTERACTIONS_TO_EM = true;
               |""".stripMargin
          )
        end if
      }
    end if
    writer.println()
    writer.println(s"""   public record Probability(float m, float u) {
         |   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
