package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomFields {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em"
  private val custom_className = "CustomFields"
  private val packageText = "org.jembi.jempi.em"

  def generate(config: Config): Any = {

    def colFieldDefs(): String =
      config.demographicFields.zipWithIndex
        .map((f, i) => {
          val colName = "COL_" + Utils.camelCaseToSnakeCase(f.fieldName)
          s"""${" " * 2}private val ${colName.toUpperCase()} = ${i}"""
        })
        .mkString(sys.props("line.separator"))
    end colFieldDefs

    def colLinkFieldDefs(): String =
      config.demographicFields
        .filter(f => f.linkMetaData.isDefined)
        .map(f => {
          val colName = "  COL_" + Utils.camelCaseToSnakeCase(f.fieldName)
          s"""${" " * 2}${colName.toUpperCase()},"""
        })
        .mkString(sys.props("line.separator"))
        .dropRight(1)
    end colLinkFieldDefs

    def colValidateFieldDefs(): String =
      config.demographicFields
        .filter(f => f.validateMetaData.isDefined)
        .map(f => {
          val colName = "  COL_" + Utils.camelCaseToSnakeCase(f.fieldName)
          s"""${" " * 2}${colName.toUpperCase()},"""
        })
        .mkString(sys.props("line.separator"))
        .dropRight(1)
    end colValidateFieldDefs

    def colMatchFieldDefs(): String =
      config.demographicFields
        .filter(f => f.matchMetaData.isDefined)
        .map(f => {
          val colName = "  COL_" + Utils.camelCaseToSnakeCase(f.fieldName)
          s"""${" " * 2}${colName.toUpperCase()},"""
        })
        .mkString(sys.props("line.separator"))
        .dropRight(1)
    end colMatchFieldDefs

    def fieldDefs(): String =
      config.demographicFields
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          val colName = "COL_" + Utils.camelCaseToSnakeCase(f.fieldName)
          s"""${" " * 4}Field("${fieldName}", ${colName.toUpperCase()}),"""
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
      |${colFieldDefs()}
      |
      |  val FIELDS: ArraySeq[Field] = ArraySeq(
      |    ${fieldDefs()}
      |  )
      |
      |  val LINK_COLS: ArraySeq[Int] = ArraySeq(
      |${colLinkFieldDefs()}
      |  )
      |
      |  val VALIDATE_COLS: ArraySeq[Int] = ArraySeq(
      |${colValidateFieldDefs()}
      |  )
      |
      |  val MATCH_COLS: ArraySeq[Int] = ArraySeq(
      |${colMatchFieldDefs()}
      |  )
      |
      |}
      |""".stripMargin)
    writer.flush()
    writer.close()
  }

}
