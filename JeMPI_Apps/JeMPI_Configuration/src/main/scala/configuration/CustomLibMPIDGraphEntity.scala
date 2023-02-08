package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIDGraphEntity {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIDGraphEntity"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    val margin = 32
    writer.println(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |import com.fasterxml.jackson.annotation.JsonProperty;
         |import org.jembi.jempi.shared.models.CustomEntity;
         |import org.jembi.jempi.libmpi.MpiEntity;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(@JsonProperty("uid") String uid,
         |${" " * margin}@JsonProperty("Entity.source_id") LibMPISourceId sourceId,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val propertyName = s"Entity.${field.fieldName}"
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = field.fieldType
        writer.println(
          s"""${" " * margin}@JsonProperty("$propertyName") $parameterType $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * margin}@JsonProperty("GoldenRecord.entity_list|score") Float score) {
         |   $customClassName(final CustomEntity entity, final Float score) {
         |      this(entity.uid(),
         |           new LibMPISourceId(entity.sourceId()),""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        writer.println(s"${" " * 11}entity.${Utils.snakeCaseToCamelCase(field.fieldName)}(),")
    }
    writer.println(
      s"""${" " * 11}score);
         |   }""".stripMargin)


    writer.println(
      """
        |   CustomEntity toCustomEntity() {
        |      return new CustomEntity(this.uid(),
        |                              this.sourceId() != null
        |                                 ? this.sourceId().toSourceId()
        |                                 : null,""".stripMargin)
    fields.zipWithIndex.foreach {
      (field, idx) =>
        writer.println(
          s"${" " * 30}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
            (if (idx + 1 < fields.length) "," else ");"))
    }
    writer.println("   }")
    writer.println(
      """
        |   MpiEntity toMpiEntity() {
        |      return new MpiEntity(toCustomEntity(), this.score());
        |   }""".stripMargin)
    writer.println(
      """
        |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
