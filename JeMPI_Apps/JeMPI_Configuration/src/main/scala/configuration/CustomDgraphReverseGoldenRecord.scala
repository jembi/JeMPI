package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphReverseGoldenRecord {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphReverseGoldenRecord"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def goldenRecordFields(config: Config): String =
    (if (config.uniqueGoldenRecordFields.isEmpty) "" else
      config
        .uniqueGoldenRecordFields
        .get
        .map(f =>
          s"""${" " * 6}@JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_${f.fieldName.toUpperCase}) ${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n") + "\n")
      +
      config
        .demographicFields
        .map(f =>
          s"""${" " * 6}@JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_${f.fieldName.toUpperCase}) ${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n")
  end goldenRecordFields

  private def addFields(writer: PrintWriter, config: Config): Unit = {
    val margin = 6
    config.demographicFields.zipWithIndex.foreach {
      case (field, idx) =>
        val propertyName = s"GoldenRecord.${field.fieldName}"
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = field.fieldType
        writer.println(
          s"""${" " * margin}@JsonProperty("$propertyName") $parameterType $parameterName,""".stripMargin)
    }
    writer.println(s"""${" " * margin}@JsonProperty("~GoldenRecord.interactions|score") Float score) {""")
  }

  private def uniqueArguments(config: Config): String =
    if (config.uniqueGoldenRecordFields.isEmpty)
      ""
    else
      config
        .uniqueGoldenRecordFields
        .get
        .map(f =>
          s"""${" " * 63}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
        .mkString("\n").trim.dropRight(1)
  end uniqueArguments

  private def demographicArguments(config: Config): String =
    config
      .demographicFields
      .map(f =>
        s"""${" " * 56}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
      .mkString("\n").trim.dropRight(1)
  end demographicArguments

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    val margin = 39
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.models.GoldenRecordWithScore;
         |import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.GoldenRecord;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String goldenId,
         |      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
         |${goldenRecordFields(config)}
         |      @JsonProperty("~GoldenRecord.interactions|score") Float score) {
         |
         |   GoldenRecord toGoldenRecord() {
         |      return new GoldenRecord(this.goldenId(),
         |                              this.sourceId() != null
         |                                    ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
         |                                    : List.of(),
         |                              new CustomUniqueGoldenRecordData(${uniqueArguments(config)}),
         |                              new CustomDemographicData(${demographicArguments(config)}));
         |   }
         |
         |   GoldenRecordWithScore toGoldenRecordWithScore() {
         |      return new GoldenRecordWithScore(toGoldenRecord(), score);
         |   }
         |
         |}
         |""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
