package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphExpandedInteraction {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphExpandedInteraction"
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
         |${interactionFields()}
         |      @JsonProperty("~GoldenRecord.interactions") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {
         |
         |   Interaction toInteraction() {
         |      return new Interaction(this.interactionId(),
         |                             this.sourceId().toSourceId(),
         |                             new CustomUniqueInteractionData(${uniqueArguments()}),
         |                             new CustomDemographicData(${demographicArguments()}));
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


    def interactionFields(): String =

      def mapField(fieldName: String, fieldType: String): String = s"""      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_${fieldName.toUpperCase}) ${Utils.javaType(fieldType)} ${Utils.snakeCaseToCamelCase(fieldName)},"""

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

    def uniqueArguments(): String =
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

    def demographicArguments(): String =
      config
        .demographicFields
        .map(f =>
          s"""${" " * 55}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
        .mkString("\n").trim.dropRight(1)
    end demographicArguments

  end generate

}
