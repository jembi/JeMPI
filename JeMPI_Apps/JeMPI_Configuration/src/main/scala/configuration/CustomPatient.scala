package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation =
    "../JeMPI_LibShared/src/main/java/org/jembi/jempi/shared/models"
  private val packageText = "org.jembi.jempi.shared.models"
  private val customClassNameCustomDemographicData = "CustomDemographicData"
  private val customClassNameCustomUniqueGoldenRecordData =
    "CustomUniqueGoldenRecordData"
  private val customClassNameCustomUniqueInteractionData =
    "CustomUniqueInteractionData"

  private val classCustomDemographicDataFile: String =
    classLocation + File.separator + customClassNameCustomDemographicData + ".java"
  private val classCustomUniqueGoldenRecordDataFile: String =
    classLocation + File.separator + customClassNameCustomUniqueGoldenRecordData + ".java"
  private val classCustomUniqueInteractionDataFile: String =
    classLocation + File.separator + customClassNameCustomUniqueInteractionData + ".java"

  private val indent = 3

  def generate(config: Config): Unit =
    generateDemographicData(config)
    generateUniqueGoldenRecordData(config)
    generateUniqueInteractionData(config)
    generateAdditionalNodes(config)
  end generate

  private def generateDemographicData(config: Config): Unit =

    def cleanedFields(config: Config): String =
      config.demographicFields
        .map(f =>
          s"""${" " * 39}this.${Utils.snakeCaseToCamelCase(
              f.fieldName
            )}.trim().toLowerCase().replaceAll("\\\\W", ""),"""
        )
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end cleanedFields

    println("Creating " + classCustomDemographicDataFile)
    val file: File = new File(classCustomDemographicDataFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public class $customClassNameCustomDemographicData {
         |""".stripMargin)
    config.demographicFields.zipWithIndex.foreach { case (field, _) =>
      val typeString = field.fieldType
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.println(
        s"""${" " * (indent * 1)}public final $typeString $fieldName;"""
      )
    }
    writer.println()
    /*    for (field <- config.demographicFields) {
      val typeString = field.fieldType
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.println(
        s"""${" " * (indent * 1)}public final $typeString get${fieldName
            .charAt(0)
            .toUpper}${fieldName.substring(1)}() {
           |${" " * (indent * 2)}return $fieldName;
           |${" " * (indent * 1)}}
           |""".stripMargin
      )
    }
     */
    writer.println(
      s"""${" " * indent * 1}public $customClassNameCustomDemographicData() {""".stripMargin
    )
    writer.println(
      s"""${" " * indent * 2}this(${"null, " * (config.demographicFields.length - 1)}null);
         |${" " * indent * 1}}
         |""".stripMargin
    )

    writer.print(
      s"""${" " * indent * 1}public $customClassNameCustomDemographicData(final $customClassNameCustomDemographicData demographicData) {"""
    )
    config.demographicFields.zipWithIndex.foreach { case (field, idx) =>
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.print(
        s"""
           |${" " * indent * 2}this.$fieldName = demographicData.$fieldName;""".stripMargin
      )
    }
    writer.println(s"""
         |${" " * indent * 1}}
         |""".stripMargin)

    writer.println(
      s"""${" " * indent * 1}public $customClassNameCustomDemographicData(""".stripMargin
    )
    config.demographicFields.zipWithIndex.foreach { case (field, idx) =>
      val typeString = field.fieldType
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.println(s"""${" " * indent * 2}final $typeString $fieldName${
                         if (idx < config.demographicFields.length - 1) ','
                         else ") {"
                       }""".stripMargin)
    }
    config.demographicFields.zipWithIndex.foreach { case (field, _) =>
      //        val typeString = field.fieldType
      val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
      writer.println(
        s"""${" " * indent * 3}this.$fieldName = $fieldName;""".stripMargin
      )
    }
    writer.println(s"""${" " * indent * 1}}
         |
         |   public $customClassNameCustomDemographicData clean() {
         |      return new $customClassNameCustomDemographicData(${cleanedFields(
                       config
                     )});
         |   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()

  end generateDemographicData

  private def generateUniqueGoldenRecordData(config: Config): Unit =

    def fields(config: Config): String =
      if (config.uniqueGoldenRecordFields.isEmpty) ""
      else
        config.uniqueGoldenRecordFields.get
          .map(f =>
            s"""${" " * 43}${Utils.javaType(f.fieldType)} ${Utils
                .snakeCaseToCamelCase(f.fieldName)},"""
          )
          .mkString(sys.props("line.separator"))
          .trim
          .dropRight(1)
    end fields

    def fromInteraction(): String =
      if (config.uniqueGoldenRecordFields.isEmpty) ""
      else
        config.uniqueGoldenRecordFields.get
          .map(f =>
            if (f.source.isEmpty) ""
            else
              s""",
               |${" " * 9}uniqueInteractionData.${Utils.snakeCaseToCamelCase(
                  f.source.get
                )}()""".stripMargin
          )
          .mkString(sys.props("line.separator"))
          .trim
    end fromInteraction

    println("Creating " + classCustomUniqueGoldenRecordDataFile)
    val file: File = new File(classCustomUniqueGoldenRecordDataFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |import java.time.LocalDateTime;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameCustomUniqueGoldenRecordData(${fields(
                     config
                   )}) {
         |
         |  public CustomUniqueGoldenRecordData(final CustomUniqueInteractionData uniqueInteractionData) {
         |    this(LocalDateTime.now(),
         |         true${fromInteraction()}
         |    );
         |  }
         |
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()
  end generateUniqueGoldenRecordData

  private def generateUniqueInteractionData(config: Config): Unit =

    def fields(config: Config): String =
      if (config.uniqueInteractionFields.isEmpty) ""
      else
        config.uniqueInteractionFields.get
          .map(f =>
            s"""${" " * 42}${Utils.javaType(f.fieldType)} ${Utils
                .snakeCaseToCamelCase(f.fieldName)},"""
          )
          .mkString(sys.props("line.separator"))
          .trim
          .dropRight(1)
    end fields

    println("Creating " + classCustomUniqueInteractionDataFile)
    val file: File = new File(classCustomUniqueInteractionDataFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public record $customClassNameCustomUniqueInteractionData(${fields(
                     config
                   )}) {
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()
  end generateUniqueInteractionData

  private def generateAdditionalNodes(config: Config): Unit =
    println("Creating additional nodes")
    if (config.additionalNodes.isDefined) {
      val nodes = config.additionalNodes.get
      nodes.foreach(n => {

        def nodeFields(): String = {
          n.fields
            .map(f =>
              s"""${" " * 6}${f.fieldType} ${f.fieldName}""".stripMargin
            )
            .mkString(s""",
                 |""")
        }

        val className = "Custom" + n.nodeName
        val fileName = classLocation + File.separator + className + ".java"
        val file: File = new File(fileName)
        val writer: PrintWriter = new PrintWriter(file)
        writer.println(s"""
             |package org.jembi.jempi.shared.models;
             |
             |import com.fasterxml.jackson.annotation.JsonInclude;
             |
             |@JsonInclude(JsonInclude.Include.NON_NULL)
             |public record $className(
             |      String uid,
             |${nodeFields()}) {
             |}
             |""".stripMargin)
        writer.flush()
        writer.close()
      })
    }
  end generateAdditionalNodes

}
