package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIDGraphGoldenRecord {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomLibMPIDGraphGoldenRecord"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def addFields(writer: PrintWriter, fields: Array[Field]): Unit = {
    val margin = 6
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val propertyName = s"GoldenRecord.${field.fieldName}"
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = field.fieldType
        writer.println(
          s"""${" " * margin}@JsonProperty("$propertyName") $parameterType $parameterName,""".stripMargin)
    }
    writer.println(s"""${" " * margin}@JsonProperty("~GoldenRecord.patients|score") Float score) {""")
  }

  def generate(fields: Array[Field]): Unit =
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
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.GoldenRecord;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String uid,
         |${" " * 6}@JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,""".stripMargin)
    addFields(writer, fields)

    writer.print(
      """
        |   GoldenRecord toGoldenRecord() {
        |      return new GoldenRecord(this.uid(),
        |                              this.sourceId() != null
        |                                    ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
        |                                    : List.of(),
        |                              new CustomDemographicData(""".stripMargin)
    fields.zipWithIndex.foreach {
      (field, idx) =>
        writer.println(
          s"${" " * (if (idx == 0) 0 else 62)}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
            (if (idx + 1 < fields.length) "," else "));"))
    }
    writer.println("   }")
    writer.println(
      """
        |   GoldenRecordWithScore toGoldenRecordWithScore() {
        |      return new GoldenRecordWithScore(toGoldenRecord(), score);
        |   }""".stripMargin)
    writer.println(
      """
        |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
