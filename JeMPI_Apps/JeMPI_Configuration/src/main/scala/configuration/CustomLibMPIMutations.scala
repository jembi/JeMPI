package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIMutations {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val custom_className = "CustomLibMPIMutations"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.utils.AppUtils;
         |
         |import java.util.UUID;
         |
         |final class $custom_className {
         |
         |${" " * 3}private $custom_className() {
         |${" " * 3}}
         |
         |${" " * 3}static String createPatientTriple(
         |${" " * 9}final CustomDemographicData demographicData,
         |${" " * 9}final String sourceUID) {
         |${" " * 6}final String uuid = UUID.randomUUID().toString();
         |${" " * 6}return String.format(\"\"\"""".stripMargin)

    // createDocumentTriple
    writer.println(s"""${" " * 27}_:%s  <PatientRecord.source_id>${" " * 16}<%s>${" " * 8}.""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = field.fieldName
        writer.println(s"""${" " * 27}_:%s  <PatientRecord.$name>${" " * (25 - name.length)}%s${" " * 10}.""".stripMargin)
    }
    writer.println(
      s"""${" " * 27}_:%s  <dgraph.type>                     \"PatientRecord\"    .
         |${" " * 27}\"\"\",""".stripMargin)
    writer.println(s"""${" " * 27}uuid, sourceUID,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = "demographicData." + field.fieldName
        writer.println(
          s"""${" " * 27}uuid, AppUtils.quotedValue(${Utils.snakeCaseToCamelCase(name)}()),""".stripMargin)
    }
    writer.println(
      s"""${" " * 27}uuid);
         |   }
         |""".stripMargin)

    // createLinkedGoldenRecordTriple
    writer.println(
      s"""   static String createLinkedGoldenRecordTriple(
         |         final CustomDemographicData demographicData,
         |         final String patientUID,
         |         final String sourceUID,
         |         final float score) {
         |      final String uuid = UUID.randomUUID().toString();
         |      return String.format(\"\"\"
         |                           _:%s  <GoldenRecord.source_id>                     <%s>             .""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = field.fieldName
        writer.println(
          s"""${" " * 27}_:%s  <GoldenRecord.$name>${" " * (30 - name.length)}%s${" " * 15}.""".stripMargin)
    }
    writer.println(
      s"""${" " * 27}_:%s  <GoldenRecord.patients>                      <%s> (score=%f)  .
         |${" " * 27}_:%s  <dgraph.type>                                "GoldenRecord"   .
         |${" " * 27}\"\"\",
         |${" " * 27}uuid, sourceUID,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = "demographicData." + Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(s"""${" " * 27}uuid, AppUtils.quotedValue($name()),""".stripMargin)
    }
    writer.println(
      s"""${" " * 27}uuid, patientUID, score,
         |${" " * 27}uuid);
         |${" " * 3}}
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
