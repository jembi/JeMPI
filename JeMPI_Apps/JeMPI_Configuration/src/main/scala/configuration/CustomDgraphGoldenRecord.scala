package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphGoldenRecord {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphGoldenRecord"
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
        .commonFields
        .map(f =>
          s"""${" " * 6}@JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_${f.fieldName.toUpperCase}) ${Utils.javaType(f.fieldType)} ${Utils.snakeCaseToCamelCase(f.fieldName)},""")
        .mkString("\n")
        .dropRight(1)
  end goldenRecordFields

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
      .commonFields
      .map(f =>
        s"""${" " * 56}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),""")
      .mkString("\n").trim.dropRight(1)
  end demographicArguments

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    val margin = 33
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.GoldenRecord;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |${" " * 6}@JsonProperty("uid") String goldenId,
         |${" " * 6}@JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
         |${goldenRecordFields(config)}) {
         |
         |   GoldenRecord toGoldenRecord() {
         |      return new GoldenRecord(this.goldenId(),
         |                              this.sourceId() != null
         |                                 ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
         |                                 : List.of(),
         |                              new CustomUniqueGoldenRecordData(${uniqueArguments(config)}),
         |                              new CustomDemographicData(${demographicArguments(config)}));
         |   }
         |
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  end generate

}
