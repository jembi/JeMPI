package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomInteractionEnvelop {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em"
  private val custom_className = "CustomInteractionEnvelop"
  private val packageText = "org.jembi.jempi.em"

  def generate(config: Config): Any = {

    val muList =
      for (t <- config.demographicFields.filter(f => f.linkMetaData.isDefined))
        yield t

    def fieldDefs(): String =
      muList.zipWithIndex
        .map((f, i) => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 4}${fieldName}: String,"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end fieldDefs

    def fieldList(): String =
      muList.zipWithIndex
        .map((f, i) => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${fieldName}"""
        })
        .mkString("", "," + sys.props("line.separator") + (" " * 12), "")
        .trim
    end fieldList

    val classFile: String =
      classLocation + File.separator + custom_className + ".scala"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    writer.println(s"package $packageText")
    writer.println()

    if (muList.length == 0) {
      writer.println(s"""
                        |import com.fasterxml.jackson.annotation.JsonIgnoreProperties
                        |
                        |
                        |@JsonIgnoreProperties(ignoreUnknown = true)
                        |case class ${custom_className}(
                        |    contentType: String,
                        |    tag: Option[String],
                        |    stan: Option[String],
                        |    interaction: Option[Interaction]
                        |) {}
                        |
                        |@JsonIgnoreProperties(ignoreUnknown = true)
                        |case class Interaction(
                        |    uniqueInteractionData: UniqueInteractionData,
                        |    demographicData: DemographicData
                        |)
                        |
                        |@JsonIgnoreProperties(ignoreUnknown = true)
                        |case class UniqueInteractionData(auxId: String)
                        |
                        |@JsonIgnoreProperties(ignoreUnknown = true)
                        |case class DemographicData(
                        |    ${fieldDefs()}
                        |) {
                        |
                        |   def toArray: Array[String] =
                        |      Array(${fieldList()})
                        |
                        |}
                        |""".stripMargin)
    } else {
      writer.println(s"""
           |import com.fasterxml.jackson.annotation.JsonIgnoreProperties
           |
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class ${custom_className}(
           |    contentType: String,
           |    tag: Option[String],
           |    stan: Option[String],
           |    interaction: Option[Interaction]
           |) {}
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class Interaction(
           |    uniqueInteractionData: UniqueInteractionData,
           |    demographicData: DemographicData
           |)
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class UniqueInteractionData(auxId: String)
           |
           |@JsonIgnoreProperties(ignoreUnknown = true)
           |case class DemographicData(
           |    ${fieldDefs()}
           |) {
           |
           |   def toArray: Array[String] =
           |      Array(${fieldList()})
           |
           |}
           |""".stripMargin)

    }
    writer.flush()
    writer.close()
  }

}
