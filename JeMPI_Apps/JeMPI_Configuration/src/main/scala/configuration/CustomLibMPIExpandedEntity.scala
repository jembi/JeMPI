package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIExpandedEntity {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIExpandedEntity"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def addFields(writer: PrintWriter, fields: Array[Field]): Unit = {
    val margin = 34
    fields.foreach {
      case field =>
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = (if field.isList.isDefined && field.isList.get then "List<" else "") +
          field.fieldType + (if field.isList.isDefined && field.isList.get then ">" else "")
        writer.println(
          s"""${" " * margin}@JsonProperty("Entity.${field.fieldName}") ${parameterType} $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * margin}@JsonProperty("~GoldenRecord.entity_list") List<CustomLibMPIDGraphGoldenRecord>
         |${" " * (margin + 6)}dgraphGoldenRecordList) {
         |""".stripMargin)
  }

  private def toCustomEntity(writer: PrintWriter, fields: Array[Field]): Unit = {
        writer.println(
          """
            |   CustomEntity toCustomEntity() {
            |      return new CustomEntity(this.uid(),
            |                              this.sourceId().toSourceId(),""".stripMargin)

        fields.zipWithIndex.foreach {
          (field, idx) =>
            writer.println(
              s"${" " * 30}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
                (if (idx + 1 < fields.length) "," else ");"))
        }
        writer.println("   }")
  }

  private def toMpiExpandedEntity(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      """
        |   MpiExpandedEntity toMpiExpandedEntity() {
        |      return new MpiExpandedEntity(this.toCustomEntity(),
        |                                   this.dgraphGoldenRecordList()
        |                                       .stream()
        |                                       .map(CustomLibMPIDGraphGoldenRecord::toMpiGoldenRecord)
        |                                       .toList());
        |   }
        |""".stripMargin)
  }

  def generate(fields: Array[Field]): Unit =
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
         |
         |import java.util.List;
         |
         |import org.jembi.jempi.shared.models.CustomEntity;
         |import org.jembi.jempi.libmpi.MpiExpandedEntity;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(@JsonProperty("uid") String uid,
         |${" " * margin}@JsonProperty("Entity.source_id") LibMPISourceId sourceId,""".stripMargin)
    addFields(writer, fields)
    //    fields.foreach {
    //      case field =>
    //        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
    //        val parameterType = (if field.isList.isDefined && field.isList.get then "List<" else "") +
    //          field.fieldType + (if field.isList.isDefined && field.isList.get then ">" else "")
    //        writer.println(s"""${" " * margin}@JsonProperty("Entity.${field.fieldName}") ${parameterType}
    //        $parameterName,""".stripMargin)
    //    }
    //    writer.println(
    //      s"""${" " * margin}@JsonProperty("~GoldenRecord.entity_list") List<CustomLibMPIDGraphGoldenRecord>
    //      dgraphGoldenRecordList) {
    //         |""".stripMargin)

    //    writer.println(
    //      """
    //        |   CustomEntity toCustomEntity() {
    //        |      return new CustomGoldenRecord(this.uid(),
    //        |                                    this.sourceId() != null
    //        |                                       ? this.sourceId().stream().map(LibMPISourceId::toSourceId)
    //        .toList()
    //        |                                       : List.of(),""".stripMargin)
    //
    //    fields.zipWithIndex.foreach {
    //      (field, idx) =>
    //        writer.println(
    //          s"${" " * 36}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
    //            (if (idx + 1 < fields.length) "," else ");"))
    //    }
    //    writer.println("   }")
    //
    toCustomEntity(writer, fields)
    toMpiExpandedEntity(writer, fields)
    //    writer.println(
    //      """
    //        |   MpiExpandedGoldenRecord toMpiExpandedEntity() {
    //        |      return new MpiExpandedGoldenRecord(this.toCustomGoldenRecord(),
    //        |                                         this.dgraphEntityList()
    //        |                                             .stream()
    //        |                                             .map(CustomLibMPIDGraphEntity::toMpiEntity)
    //        |                                             .toList());
    //        |   }
    //        |""".stripMargin)

    writer.println("}")
    writer.flush()
    writer.close()
  end generate

}
