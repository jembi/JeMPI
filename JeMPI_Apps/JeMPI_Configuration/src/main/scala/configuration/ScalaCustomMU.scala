package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomMU {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em/kafka"
  private val custom_className = "CustomMU"
  private val packageText = "org.jembi.jempi.em.kafka"

  def generate(config: Config): Any = {

    def linkFieldDefs(): String =
      config.demographicFields
        .filter(f => f.linkMetaData.isDefined)
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 2}${fieldName}: Probability,"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end linkFieldDefs

    def validateFieldDefs(): String =
      config.demographicFields
        .filter(f => f.validateMetaData.isDefined)
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 2}${fieldName}: Probability,"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end validateFieldDefs

    def matchFieldDefs(): String =
      config.demographicFields
        .filter(f => f.matchMetaData.isDefined)
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 2}${fieldName}: Probability,"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end matchFieldDefs

    def linkProbSeqDefs(): String =
      config.demographicFields
        .filter(f => f.linkMetaData.isDefined)
        .zipWithIndex
        .map((f, i) => {
          s"""${" " * 8}Probability(muSeqLink.apply(${i}).m, muSeqLink.apply(${i}).u),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end linkProbSeqDefs

    def validateProbSeqDefs(): String =
      config.demographicFields
        .filter(f => f.validateMetaData.isDefined)
        .zipWithIndex
        .map((f, i) => {
          s"""${" " * 8}Probability(muSeqValidate.apply(${i}).m, muSeqValidate.apply(${i}).u),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end validateProbSeqDefs

    def matchProbSeqDefs(): String =
      config.demographicFields
        .filter(f => f.matchMetaData.isDefined)
        .zipWithIndex
        .map((f, i) => {
          s"""${" " * 8}Probability(muSeqMatch.apply(${i}).m, muSeqMatch.apply(${i}).u),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end matchProbSeqDefs

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
         |  tag: String,
         |  customLinkMU: CustomLinkMU,
         |  customValidateMU: CustomValidateMU,
         |  customMatchMU: CustomMatchMU
         |)
         |
         |case class CustomLinkMU(
         |  ${linkFieldDefs()}
         |)
         |
         |case class CustomValidateMU(
         |  ${validateFieldDefs()}
         |)
         |
         |case class CustomMatchMU(
         |  ${matchFieldDefs()}
         |)
         |
         |object ${custom_className} {
         |
         |  def fromArraySeq(tag: String, muSeqLink: ArraySeq[MU], muSeqValidate: ArraySeq[MU], muSeqMatch: ArraySeq[MU]): CustomMU =
         |    CustomMU(
         |      tag,
         |      CustomLinkMU(
         |        ${linkProbSeqDefs()}
         |      ),
         |      CustomValidateMU(
         |        ${validateProbSeqDefs()}
         |      ),
         |      CustomMatchMU(
         |        ${matchProbSeqDefs()}
         |      )
         |    )
         |
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  }

}
