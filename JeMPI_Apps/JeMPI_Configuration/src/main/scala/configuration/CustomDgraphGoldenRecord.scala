package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphGoldenRecord {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphGoldenRecord"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(fields: Array[Field]): Unit =
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
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.GoldenRecord;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |${" " * 6}@JsonProperty("uid") String goldenId,
         |${" " * 6}@JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val propertyName = "GoldenRecord." + field.fieldName
        val parameterType =
          (if (field.isList.isDefined && field.isList.get) "List<" else "") +
            field.fieldType +
            (if (field.isList.isDefined && field.isList.get) ">" else "")
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(
          s"""${" " * 6}@JsonProperty("$propertyName") $parameterType $parameterName${
            if (idx + 1 < fields.length) ","
            else ") {"
          }""".stripMargin)
    }
    writer.println(
      s"""
         |${" " * 3}$customClassName(final CustomDgraphInteraction rec) {
         |${" " * 6}this(null,
         |${" " * 11}List.of(rec.sourceId()),""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val arg = (if (field.isList.isDefined && field.isList.get) "List.of(" else "") +
          "rec." + Utils.snakeCaseToCamelCase(field.fieldName) +
          "()" +
          (if (field.isList.isDefined && field.isList.get) ")" else "")
        writer.println(
          s"""${" " * 11}$arg${if (idx + 1 < fields.length) "," else ");"}""".stripMargin)
    }
    writer.println(s"   }")

    writer.print(
      s"""
         |   GoldenRecord toGoldenRecord() {
         |      return new GoldenRecord(this.goldenId(),
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
    writer.println()
//    writer.println(
//      """   MpiGoldenRecord toMpiGoldenRecord() {
//        |      return new MpiGoldenRecord(this.toCustomGoldenRecord());
//        |   }
//        |""".stripMargin)
    writer.println("}")
    writer.flush()
    writer.close()
  end generate

}
