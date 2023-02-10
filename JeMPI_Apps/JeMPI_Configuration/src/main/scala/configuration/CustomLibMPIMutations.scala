package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIMutations {

  private val classLocation = "../JeMPI_Shared_Source/custom"
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
         |import java.util.UUID;
         |
         |import org.jembi.jempi.shared.models.CustomPatient;
         |import org.jembi.jempi.shared.utils.AppUtils;
         |
         |class $custom_className {
         |
         |${" " * 3}private $custom_className() {}
         |
         |${" " * 3}static String createPatientTriple(final CustomPatient patient, final String sourceUID) {
         |${" " * 6}final String uuid = UUID.randomUUID().toString();
         |${" " * 6}return String.format(
         |${" " * 9}\"\"\"""".stripMargin)

    // createDocumentTriple
    writer.println(s"""${" " * 9}_:%s  <Patient.source_id>${" " * 16}<%s>${" " * 8}.""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = field.fieldName
        writer.println(s"""${" " * 9}_:%s  <Patient.$name>${" " * (25 - name.length)}%s${" " * 10}.""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}_:%s  <dgraph.type>                     \"Patient\"    .
         |${" " * 9}\"\"\",""".stripMargin)
    writer.println(s"""${" " * 9}uuid, sourceUID,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = "patient." + field.fieldName
        writer.println(
          s"""${" " * 9}uuid, AppUtils.quotedValue(${Utils.snakeCaseToCamelCase(name)}()),""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}uuid);
         |   }
         |""".stripMargin)

    // createLinkedGoldenRecordTriple
    writer.println(
      s"""   static String createLinkedGoldenRecordTriple(final CustomPatient patient,
         |                                                final String patientUID,
         |                                                final String sourceUID,
         |                                                final float score) {
         |      final String uuid = UUID.randomUUID().toString();
         |      return String.format(
         |         \"\"\"
         |         _:%s  <GoldenRecord.source_id>                     <%s>             .""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = field.fieldName
        writer.println(
          s"""${" " * 9}_:%s  <GoldenRecord.$name>${" " * (30 - name.length)}%s${" " * 15}.""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}_:%s  <GoldenRecord.patients>                      <%s> (score=%f)  .
         |${" " * 9}_:%s  <dgraph.type>                                "GoldenRecord"   .
         |${" " * 9}\"\"\",
         |${" " * 9}uuid, sourceUID,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = "patient." + Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(s"""${" " * 9}uuid, AppUtils.quotedValue($name()),""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}uuid, patientUID, score,
         |${" " * 9}uuid);
         |${" " * 3}}
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
