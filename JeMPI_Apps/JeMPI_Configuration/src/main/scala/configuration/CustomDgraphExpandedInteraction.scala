package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphExpandedInteraction {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphExpandedInteraction"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def interactionFields(config: Config): String =
    (if (config.uniqueInteractionFields.isEmpty) "" else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          s"""      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_${f.fieldName.toUpperCase}) ${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n") + "\n")
      +
      config
        .commonFields
        .map(f =>
          s"""      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_${f.fieldName.toUpperCase}) ${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n")
  end interactionFields

  private def uniqueArguments(config: Config): String =
    if (config.uniqueInteractionFields.isEmpty)
      ""
    else
      config
        .uniqueInteractionFields
        .get
        .map(f =>
          s"""${" " * 63}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
        .mkString("\n").trim.dropRight(1)
  end uniqueArguments

  private def demographicArguments(config: Config): String =
    config
      .commonFields
      .map(f =>
        s"""${" " * 55}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
      .mkString("\n").trim.dropRight(1)
  end demographicArguments

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    val margin = 34
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.ExpandedInteraction;
         |import org.jembi.jempi.shared.models.Interaction;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String interactionId,
         |      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
         |${interactionFields(config)}
         |      @JsonProperty("~GoldenRecord.interactions") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {
         |
         |   Interaction toInteraction() {
         |      return new Interaction(this.interactionId(),
         |                             this.sourceId().toSourceId(),
         |                             new CustomUniqueInteractionData(${uniqueArguments(config)}),
         |                             new CustomDemographicData(${demographicArguments(config)}));
         |   }
         |
         |   ExpandedInteraction toExpandedInteraction() {
         |      return new ExpandedInteraction(this.toInteraction(),
         |                                     this.dgraphGoldenRecordList()
         |                                         .stream()
         |                                         .map(CustomDgraphReverseGoldenRecord::toGoldenRecordWithScore)
         |                                         .toList());
         |   }
         |
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
