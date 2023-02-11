package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIExpandedPatient {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIExpandedPatient"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def addFields(writer: PrintWriter, fields: Array[Field]): Unit = {
    val margin = 6
    fields.foreach {
      case field =>
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = (if field.isList.isDefined && field.isList.get then "List<" else "") +
          field.fieldType + (if field.isList.isDefined && field.isList.get then ">" else "")
        writer.println(
          s"""${" " * margin}@JsonProperty("Patient.${field.fieldName}") ${parameterType} $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * margin}@JsonProperty("~GoldenRecord.patients") List<CustomLibMPIDGraphGoldenRecord> dgraphGoldenRecordList) {
         |""".stripMargin)
  }

  private def toCustomPatient(writer: PrintWriter, fields: Array[Field]): Unit = {
        writer.println(
          """
            |   CustomPatient toCustomPatient() {
            |      return new CustomPatient(this.uid(),
            |                               this.sourceId().toSourceId(),
            |                               new CustomDemographicData(""".stripMargin)

        fields.zipWithIndex.foreach {
          (field, idx) =>
            writer.println(
              s"${" " * 37}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
                (if (idx + 1 < fields.length) "," else "));"))
        }
        writer.println("   }")
  }

  private def toMpiExpandedPatient(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      """
        |   MpiExpandedPatient toMpiExpandedPatient() {
        |      return new MpiExpandedPatient(this.toCustomPatient(),
        |                                    this.dgraphGoldenRecordList()
        |                                        .stream()
        |                                        .map(CustomLibMPIDGraphGoldenRecord::toMpiGoldenRecord)
        |                                        .toList());
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
         |import org.jembi.jempi.libmpi.MpiExpandedPatient;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.CustomPatient;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |${" " * 6}@JsonProperty("uid") String uid,
         |${" " * 6}@JsonProperty("Patient.source_id") LibMPISourceId sourceId,""".stripMargin)
    addFields(writer, fields)
    toCustomPatient(writer, fields)
    toMpiExpandedPatient(writer, fields)
    writer.println("}")
    writer.flush()
    writer.close()
  end generate

}
