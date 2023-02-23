package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphExpandedGoldenRecord {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphExpandedGoldenRecord"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    val margin = 40
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.models.ExpandedGoldenRecord;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.GoldenRecord;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |${" " * 6}@JsonProperty("uid") String uid,
         |${" " * 6}@JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,""".stripMargin)
    fields.foreach {
      case field =>
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = (if field.isList.isDefined && field.isList.get then "List<" else "") +
          field.fieldType + (if field.isList.isDefined && field.isList.get then ">" else "")
        writer.println(s"""${" " * 6}@JsonProperty("GoldenRecord.${field.fieldName}") ${parameterType} $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * 6}@JsonProperty("GoldenRecord.patients") List<CustomDgraphPatientRecord> patients) {
         |""".stripMargin)

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
          s"${" " * (if (idx == 0) 0 else 56)}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
            (if (idx + 1 < fields.length) "," else "));"))
    }
    writer.println("   }")

    writer.println(
      """
        |   ExpandedGoldenRecord toExpandedGoldenRecord() {
        |      return new ExpandedGoldenRecord(this.toGoldenRecord(),
        |                                      this.patients().stream().map(CustomDgraphPatientRecord::toPatientRecordWithScore).toList());
        |   }
        |""".stripMargin)

    writer.println("}")
    writer.flush()
    writer.close()
  end generate

}
