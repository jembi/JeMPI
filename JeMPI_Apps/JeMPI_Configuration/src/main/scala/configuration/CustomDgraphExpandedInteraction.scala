package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphExpandedInteraction {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphExpandedInteraction"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def addFields(writer: PrintWriter, fields: Array[Field]): Unit = {
    val margin = 6
    fields.foreach {
      case field =>
        val parameterName = Utils.snakeCaseToCamelCase(field.fieldName)
        val parameterType = (if field.isList.isDefined && field.isList.get then "List<" else "") +
          field.fieldType + (if field.isList.isDefined && field.isList.get then ">" else "")
        writer.println(
          s"""${" " * margin}@JsonProperty("PatientRecord.${field.fieldName}") ${parameterType} $parameterName,""".stripMargin)
    }
    writer.println(
      s"""${" " * margin}@JsonProperty("~GoldenRecord.patients") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {
         |""".stripMargin)
  }

  private def toInteraction(writer: PrintWriter, fields: Array[Field]): Unit = {
        writer.println(
          """   Interaction toInteraction() {
            |      return new Interaction(this.patientId(),
            |                             this.sourceId().toSourceId(),
            |                             new CustomDemographicData(""".stripMargin)

        fields.zipWithIndex.foreach {
          (field, idx) =>
            writer.println(
              s"${" " * 37}this.${Utils.snakeCaseToCamelCase(field.fieldName)}()" +
                (if (idx + 1 < fields.length) "," else "));"))
        }
        writer.println(
          s"""   }
             |""".stripMargin)
  }

  private def toExpandedInteraction(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      """   ExpandedInteraction toExpandedInteraction() {
        |      return new ExpandedInteraction(this.toInteraction(),
        |                                     this.dgraphGoldenRecordList()
        |                                         .stream()
        |                                         .map(CustomDgraphReverseGoldenRecord::toGoldenRecordWithScore)
        |                                         .toList());
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
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.ExpandedInteraction;
         |import org.jembi.jempi.shared.models.Interaction;
         |
         |import java.util.List;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |record $customClassName(
         |${" " * 6}@JsonProperty("uid") String patientId,
         |${" " * 6}@JsonProperty("PatientRecord.source_id") DgraphSourceId sourceId,""".stripMargin)
    addFields(writer, fields)
    toInteraction(writer, fields)
    toExpandedInteraction(writer, fields)
    writer.println("}")
    writer.flush()
    writer.close()
  end generate

}
