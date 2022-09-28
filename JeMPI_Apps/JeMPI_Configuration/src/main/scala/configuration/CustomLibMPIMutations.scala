package org.jembi.jempi
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
         |import org.jembi.jempi.shared.models.CustomEntity;
         |import org.jembi.jempi.shared.utils.AppUtils;
         |
         |class $custom_className {
         |
         |${" " * 3}private $custom_className() {}
         |
         |${" " * 3}static String createEntityTriple(final CustomEntity customEntity, final String sourceIdUid) {
         |${" " * 6}final String uuid = UUID.randomUUID().toString();
         |${" " * 6}return String.format(
         |${" " * 9}\"\"\"""".stripMargin)

    // createDocumentTriple
    writer.println(s"""${" " * 9}_:%s  <Entity.source_id>${" " * 16}<%s>${" " * 8}.""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = field.fieldName
        writer.println(s"""${" " * 9}_:%s  <Entity.$name>${" " * (25 - name.length)}%s${" " * 10}.""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}_:%s  <dgraph.type>                     \"Entity\"    .
         |${" " * 9}\"\"\",""".stripMargin)
    writer.println(s"""${" " * 9}uuid, sourceIdUid,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = "customEntity." + field.fieldName
        writer.println(
          s"""${" " * 9}uuid, AppUtils.quotedValue(${Utils.snakeCaseToCamelCase(name)}()),""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}uuid);
         |   }
         |""".stripMargin)

    // createLinkedGoldenRecordTriple
    writer.println(
      s"""   static String createLinkedGoldenRecordTriple(final CustomEntity customEntity,
         |                                                final String entityUid,
         |                                                final String sourceUid,
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
      s"""${" " * 9}_:%s  <GoldenRecord.entity_list>                   <%s> (score=%f)  .
         |${" " * 9}_:%s  <dgraph.type>                                "GoldenRecord"   .
         |${" " * 9}\"\"\",
         |${" " * 9}uuid, sourceUid,""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = "customEntity." + Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(s"""${" " * 9}uuid, AppUtils.quotedValue($name()),""".stripMargin)
    }
    writer.println(
      s"""${" " * 9}uuid, entityUid, score,
         |${" " * 9}uuid);
         |${" " * 3}}
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
