package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphInteraction {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphInteraction"
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
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String interactionId,
         |      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
         |${interactionFields()}
         |      @JsonProperty("GoldenRecord.interactions|score") Float score) {
         |
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()

    def interactionFields(): String =

      def mapField(
          predicate: String,
          fieldName: String,
          fieldType: String
      ): String =
        s"""${" " * 6}@JsonProperty($predicate) ${Utils
            .javaType(fieldType)} ${Utils.snakeCaseToCamelCase(fieldName)},"""

      val f1 =
        if (config.uniqueInteractionFields.isEmpty) ""
        else
          config.uniqueInteractionFields.get
            .map(f =>
              mapField(
                s"""DGraphConfig.PREDICATE_INTERACTION_${f.fieldName.toUpperCase}""",
                f.fieldName,
                f.fieldType
              )
            )
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 = config.demographicFields.zipWithIndex
        .map((f, i) =>
          mapField(
            s"""${"\"Interaction.demographic_field_%02d\"".format(i)}""",
            f.fieldName,
            f.fieldType
          )
        )
        .mkString(sys.props("line.separator"))

      f1 + f2
    end interactionFields

    def interactionConstructorArguments(): String =
      val f1 =
        if (config.uniqueInteractionFields.isEmpty) ""
        else
          config.uniqueInteractionFields.get
            .map(f =>
              s"""${" " * 11}interaction.uniqueInteractionData().${Utils
                  .snakeCaseToCamelCase(f.fieldName)}(),"""
            )
            .mkString(sys.props("line.separator")) + sys.props("line.separator")

      val f2 = config.demographicFields.zipWithIndex
        .map((_, idx) =>
          s"""${" " * 11}interaction.demographicData().fields.get($idx).value(),"""
        )
        .mkString(sys.props("line.separator"))

      f1 + f2
    end interactionConstructorArguments

    def uniqueArguments(): String =
      if (config.uniqueInteractionFields.isEmpty)
        ""
      else
        config.uniqueInteractionFields.get
          .map(f =>
            s"""${" " * 63}this.${Utils.snakeCaseToCamelCase(f.fieldName)},"""
          )
          .mkString(sys.props("line.separator"))
          .trim
          .dropRight(1)
      end if
    end uniqueArguments

    def demographicArguments(): String =
      config.demographicFields
        .map(f =>
          s"""${" " * 79}this.${Utils.snakeCaseToCamelCase(f.fieldName)},"""
        )
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end demographicArguments

  end generate

}
