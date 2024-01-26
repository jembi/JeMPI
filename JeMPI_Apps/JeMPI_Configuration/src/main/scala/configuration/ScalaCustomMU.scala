package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomMU {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em/kafka"
  private val custom_className = "CustomMU"
  private val packageText = "org.jembi.jempi.em.kafka"

  def generate(config: Config): Any = {

    def fieldDefs(): String =
      config.demographicFields.zipWithIndex
        .map((f, i) => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 4}${fieldName}: Probability,"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end fieldDefs

    def probSeqDefs(): String =
      config.demographicFields.zipWithIndex
        .map((f, i) => {
          s"""${" " * 12}Probability(muSeq.apply(${i}).m, muSeq.apply(${i}).u),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end probSeqDefs

    val classFile: String =
      classLocation + File.separator + custom_className + ".scala"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val muList =
      for (t <- config.demographicFields.filter(f => f.linkMetaData.isDefined))
        yield t

    writer.println(s"""package $packageText
         |
         |
         |import org.jembi.jempi.em.MU
                  |
         |import scala.collection.immutable.ArraySeq
         |
         |case class ${custom_className}(
         |    tag: String,
         |    ${fieldDefs()}
         |)
         |
         |object ${custom_className} {
         |
         |    def fromArraySeq(tag: String, muSeq: ArraySeq[MU]): CustomMU =
         |        CustomMU(
         |            tag,
         |            ${probSeqDefs()}
         |        )
         |
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  }

}
