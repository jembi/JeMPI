package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomFields {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em"
  private val custom_className = "CustomFields"
  private val packageText = "org.jembi.jempi.em"

  def generate(config: Config): Any = {

    def fieldDefs(): String =
      config.demographicFields.zipWithIndex
        .map((f, i) => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 4}Field("${fieldName}", ${i}),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end fieldDefs

    val classFile: String =
      classLocation + File.separator + custom_className + ".scala"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    writer.println(s"""package $packageText
      |
      |import scala.collection.immutable.ArraySeq
      |
      |object CustomFields {
      |
      |  val FIELDS: ArraySeq[Field] = ArraySeq(
      |    ${fieldDefs()}
      |  )
      |
      |}
      |""".stripMargin)
    writer.flush()
    writer.close()
  }

}
