package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIDGraphGoldenRecord {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIDGraphGoldenRecord"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def addFields(writer: PrintWriter, fields: Array[Field]): Unit = {
    val margin = 39
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        val propertyName = s"GoldenRecord.${field.fieldName}"
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = field.fieldType
        writer.println(
          s"""${" " * margin}@JsonProperty("$propertyName") $parameterType $parameterName,""".stripMargin)
    }
    writer.println(s"""${" " * margin}@JsonProperty("~GoldenRecord.entity_list|score") Float score) {""")
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
         |
         |import org.jembi.jempi.shared.models.CustomGoldenRecord;
         |import org.jembi.jempi.libmpi.MpiGoldenRecord;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(@JsonProperty("uid") String uid,
         |${" " * margin}@JsonProperty("GoldenRecord.source_id") List<LibMPISourceId> sourceId,""".stripMargin)
    addFields(writer, fields)
    //    fields.zipWithIndex.foreach {
    //      case (field, idx) =>
    //        val propertyName = s"GoldenRecord.${field.fieldName}"
    //        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
    //        val parameterType = field.fieldType
    //        writer.println(
    //          s"""${" " * margin}@JsonProperty("$propertyName") $parameterType $parameterName,""".stripMargin)
    //    }
    //
    //    writer.println(
    //      s"""${" " * margin}@JsonProperty("~GoldenRecord.entity_list|score") Float score) {"""
    //    )

//    writer.println(
//      s"""
//         |   $customClassName(final CustomGoldenRecord goldenRecord, final Float score) {
//         |      this(goldenRecord.uid(),
//         |           goldenRecord.sourceId() != null
//         |                                ? goldenRecord.sourceId.stream.map(LibMPISourceId::toSourceId).toList()
//         |                                : List.of(),""".stripMargin)
//    fields.zipWithIndex.foreach {
//      case (field, _) =>
//        writer.println(s"${" " * 11}goldenRecord.${Utils.snakeCaseToCamelCase(field.fieldName)}(),")
//    }
//    writer.println(
//      s"""${" " * 11}score);
//         |   }""".stripMargin)
//

    writer.println(
      """
        |   CustomGoldenRecord toCustomGoldenRecord() {
        |      return new CustomGoldenRecord(this.uid(),
        |                                    this.sourceId() != null
        |                                       ? this.sourceId().stream().map(LibMPISourceId::toSourceId).toList()
        |                                       : List.of(),""".stripMargin)
    fields.zipWithIndex.foreach {
      (field, idx) =>
        writer.println(
          s"${" " * 36}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
            (if (idx + 1 < fields.length) "," else ");"))
    }
    writer.println("   }")
    writer.println(
      """
        |   MpiGoldenRecord toMpiGoldenRecord() {
        |      return new MpiGoldenRecord(toCustomGoldenRecord(), score);
        |   }""".stripMargin)
    writer.println(
      """
        |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
