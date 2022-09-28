package org.jembi.jempi
package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIExpandedGoldenRecord {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIExpandedGoldenRecord"
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
         |
         |import java.util.List;
         |
         |import org.jembi.jempi.shared.models.CustomGoldenRecord;
         |import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(@JsonProperty("uid") String uid,
         |${" " * margin}@JsonProperty("GoldenRecord.source_id") List<LibMPISourceId> sourceId,""".stripMargin)
    fields.foreach {
      case field =>
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = (if field.isList.isDefined && field.isList.get then "List<" else "") +
          field.fieldType + (if field.isList.isDefined && field.isList.get then ">" else "")
        writer.println(s"""${" " * margin}@JsonProperty("GoldenRecord.${field.fieldName}") ${parameterType} $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * margin}@JsonProperty("GoldenRecord.entity_list") List<CustomLibMPIDGraphEntity> dgraphEntityList) {
         |""".stripMargin)

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
        |   MpiExpandedGoldenRecord toMpiExpandedGoldenRecord() {
        |      return new MpiExpandedGoldenRecord(this.toCustomGoldenRecord(),
        |                                         this.dgraphEntityList()
        |                                             .stream()
        |                                             .map(CustomLibMPIDGraphEntity::toMpiEntity)
        |                                             .toList());
        |   }
        |""".stripMargin)

    writer.println("}")
    writer.flush()
    writer.close()
  end generate

}
