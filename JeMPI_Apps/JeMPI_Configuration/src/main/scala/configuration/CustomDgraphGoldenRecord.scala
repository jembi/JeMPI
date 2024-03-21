package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphGoldenRecord {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphGoldenRecord"
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
         |${goldenRecordFields()}) {
         |
         |   GoldenRecord toGoldenRecord() {
         |      return new GoldenRecord(this.goldenId(),
         |                              this.sourceId() != null
         |                                 ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
         |                                 : List.of(),
         |                              new CustomUniqueGoldenRecordData(${uniqueArguments()}),
         |                              CustomDemographicData.fromCustomDemographicFields(${demographicArguments()}));
         |   }
         |
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()

    def goldenRecordFields(): String =

      def field(fieldName: String, fieldType: String): String =
        s"""${" " * 6}@JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_${fieldName.toUpperCase}) ${Utils
            .javaType(fieldType)} ${Utils.snakeCaseToCamelCase(fieldName)},"""

      val f1 =
        (if (config.uniqueGoldenRecordFields.isEmpty) ""
         else
           config.uniqueGoldenRecordFields.get
             .map(f => field(f.fieldName, f.fieldType))
             .mkString(sys.props("line.separator")) + sys.props(
             "line.separator"
           ))
      val f2 =
        config.demographicFields
          .map(f => field(f.fieldName, f.fieldType))
          .mkString(sys.props("line.separator"))
          .dropRight(1)
      f1 + f2
    end goldenRecordFields

    def uniqueArguments(): String =
      if (config.uniqueGoldenRecordFields.isEmpty) ""
      else
        config.uniqueGoldenRecordFields.get
          .map(f =>
            s"""${" " * 63}this.${Utils.snakeCaseToCamelCase(f.fieldName)}(),"""
          )
          .mkString(sys.props("line.separator"))
          .trim
          .dropRight(1)
      end if
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
