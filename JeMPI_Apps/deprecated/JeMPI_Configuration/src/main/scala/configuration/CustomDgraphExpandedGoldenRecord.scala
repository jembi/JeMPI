package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphExpandedGoldenRecord {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphExpandedGoldenRecord"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(config: Config): Unit =
    val classFile: String =
      classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.config.DGraphConfig;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String goldenId,
         |      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
         |${goldenRecordFields()}
         |      @JsonProperty("GoldenRecord.interactions") List<CustomDgraphInteraction> interactions) {
         |""".stripMargin)

    writer.println("}")
    writer.flush()
    writer.close()

    def goldenRecordFields(): String =

      def mapField(
          predicate: String,
          fieldName: String,
          fieldType: String
      ): String =
        s"""${" " * 6}@JsonProperty($predicate) ${Utils.javaType(
            fieldType
          )} ${Utils.snakeCaseToCamelCase(fieldName)},"""

      val f1 =
        if (config.uniqueGoldenRecordFields.isEmpty) ""
        else
          config.uniqueGoldenRecordFields.get
            .map(f =>
              mapField(
                s"""DGraphConfig.PREDICATE_GOLDEN_RECORD_${f.fieldName.toUpperCase}""",
                f.fieldName,
                f.fieldType
              )
            )
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 =
        config.demographicFields.zipWithIndex
          .map((f, i) =>
            mapField(
              s"""${"\"GoldenRecord.demographic_field_%02d".format(i)}\"""",
              f.fieldName,
              f.fieldType
            )
          )
          .mkString(sys.props("line.separator"))

      f1 + f2

    end goldenRecordFields

    def uniqueArguments(): String =
      if (config.uniqueGoldenRecordFields.isEmpty)
        ""
      else
        config.uniqueGoldenRecordFields.get
          .map(f =>
            s"""${" " * 63}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),"""
          )
          .mkString(sys.props("line.separator"))
          .trim
          .dropRight(1)

    end uniqueArguments

    def demographicArguments(): String =
      config.demographicFields
        .map(f =>
          s"""${" " * 80}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),"""
        )
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end demographicArguments

  end generate

}
