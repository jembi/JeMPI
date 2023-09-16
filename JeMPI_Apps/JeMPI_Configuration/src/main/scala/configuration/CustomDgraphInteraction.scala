package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphInteraction {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphInteraction"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.models.InteractionWithScore;
         |import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.Interaction;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String interactionId,
         |      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
         |${interactionFields()}
         |      @JsonProperty("GoldenRecord.interactions|score") Float score) {
         |
         |   $customClassName(
         |         final Interaction interaction,
         |         final Float score) {
         |      this(interaction.interactionId(),
         |           new DgraphSourceId(interaction.sourceId()),
         |${interactionConstructorArguments()}
         |           score);
         |   }
         |
         |   Interaction toInteraction() {
         |      return new Interaction(this.interactionId(),
         |                             this.sourceId() != null
         |                                   ? this.sourceId().toSourceId()
         |                                   : null,
         |                             new CustomUniqueInteractionData(${uniqueArguments()}),
         |                             new CustomDemographicData(${demographicArguments()}));
         |   }
         |
         |   InteractionWithScore toInteractionWithScore() {
         |      return new InteractionWithScore(toInteraction(), this.score());
         |   }
         |
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()

    def interactionFields(): String =

      def mapField(fieldName: String, fieldType: String): String = s"""${" " * 6}@JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_${fieldName.toUpperCase}) ${Utils.javaType(fieldType)} ${Utils.snakeCaseToCamelCase(fieldName)},"""

      val f1 = if (config.uniqueInteractionFields.isEmpty) "" else
        config
          .uniqueInteractionFields
          .get
          .map(f => mapField(f.fieldName, f.fieldType))
          .mkString("\n") + "\n"

      val f2 = config
        .demographicFields
        .map(f => mapField(f.fieldName, f.fieldType))
        .mkString("\n")

      f1 + f2
    end interactionFields

    def interactionConstructorArguments(): String =
      val f1 = if (config.uniqueInteractionFields.isEmpty) "" else
        config
          .uniqueInteractionFields
          .get
          .map(f => s"""${" " * 11}interaction.uniqueInteractionData().${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
          .mkString("\n") + "\n"

      val f2 = config
        .demographicFields
        .map(f => s"""${" " * 11}interaction.demographicData().${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n")

      f1 + f2
    end interactionConstructorArguments

    def uniqueArguments(): String =
      if (config.uniqueInteractionFields.isEmpty)
        ""
      else
        config
          .uniqueInteractionFields
          .get
          .map(f => s"""${" " * 63}this.${Utils.snakeCaseToCamelCase(f.fieldName)},""")
          .mkString("\n")
          .trim
          .dropRight(1)
      end if
    end uniqueArguments

    def demographicArguments(): String =
      config
        .demographicFields
        .map(f => s"""${" " * 55}this.${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n")
        .trim
        .dropRight(1)
    end demographicArguments

  end generate

}