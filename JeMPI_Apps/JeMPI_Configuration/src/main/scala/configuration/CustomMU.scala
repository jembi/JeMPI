package configuration

import configuration.Config

import java.io.{File, PrintWriter}

private object CustomMU {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomMU"
  private val packageSharedModels = "org.jembi.jempi.shared.models"

  def generate(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageSharedModels;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassName(""".stripMargin)
    val margin = 23
    val filteredFields = fields.filter(_.m.isDefined)
    if (filteredFields.length == 0)
      writer.println("Probability dummy) {")
    else
      filteredFields.zipWithIndex.foreach {
        case (f, i) =>
          val parameterName = Utils.snakeCaseToCamelCase(f.fieldName)
          if (i > 0)
            writer.print(" " * margin)
          end if
          writer.print(s"Probability $parameterName")
          if (i + 1 < filteredFields.length)
            writer.println(",")
          else
            writer.println(") {")
          end if
      }
    end if
    writer.println()
    writer.println(s"   public $customClassName(final double[] mHat, final double[] uHat) {")
    if (filteredFields.length == 0)
      writer.println(s"      this(new $customClassName.Probability(0.0F, 0.0F));")
    else
      filteredFields.zipWithIndex.foreach {
        case (_, idx) =>
          val arg = s"new $customClassName.Probability((float) mHat[$idx], (float) uHat[$idx])"
          if (idx == 0)
            writer.println("      this(" + arg + ",")
          else
            writer.print(" " * 11)
            if (idx + 1 < filteredFields.length)
              writer.println(s"$arg,")
            else
              writer.println(s"$arg);")
            end if
          end if
      }
    end if
    writer.println(
      s"""   }
         |
         |   public record Probability(float m, float u) {}
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
