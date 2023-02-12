package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIDGraphPatientRecord {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIDGraphPatientRecord"
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
         |import org.jembi.jempi.shared.models.RatedPatientRecord;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.PatientRecord;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |      @JsonProperty("uid") String uid,
         |      @JsonProperty("Patient.source_id") LibMPISourceId sourceId,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val propertyName = s"Patient.${field.fieldName}"
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = field.fieldType
        writer.println(
          s"""${" " * 6}@JsonProperty("$propertyName") $parameterType $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * 6}@JsonProperty("GoldenRecord.patients|score") Float score) {
         |   $customClassName(
         |         final PatientRecord patientRecord,
         |         final Float score) {
         |      this(patientRecord.uid(),
         |           new LibMPISourceId(patientRecord.sourceId()),""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        writer.println(s"${" " * 11}patientRecord.demographicData().${Utils.snakeCaseToCamelCase(field.fieldName)}(),")
    }
    writer.println(
      s"""${" " * 11}score);
         |   }""".stripMargin)


    writer.print(
      """
        |   PatientRecord toPatientRecord() {
        |      return new PatientRecord(this.uid(),
        |                               this.sourceId() != null
        |                                     ? this.sourceId().toSourceId()
        |                                     : null,
        |                               new CustomDemographicData(""".stripMargin)
    fields.zipWithIndex.foreach {
      (field, idx) =>
        writer.println(
          s"${" " * (if (idx == 0) 0 else 57)}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
            (if (idx + 1 < fields.length) "," else "));"))
    }
    writer.println("   }")
    writer.println(
      """
        |   RatedPatientRecord toRatedPatientRecord() {
        |      return new RatedPatientRecord(toPatientRecord(), this.score());
        |   }""".stripMargin)
    writer.println(
      """
        |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
