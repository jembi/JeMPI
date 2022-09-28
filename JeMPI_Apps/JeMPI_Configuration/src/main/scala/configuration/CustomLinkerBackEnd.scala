package org.jembi.jempi
package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerBackEnd {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val custom_className = "CustomLinkerBackEnd"
  private val packageText = "org.jembi.jempi.linker"

  def parseRules(config: Config): Any = {
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    config.fields.filter(f => f.m.isDefined && f.u.isDefined).foreach(f => {
      var t = (f.fieldName, f.m.get, f.u.get)
    })
    val muList = for (
      t <- config.fields.filter(f => f.m.isDefined && f.u.isDefined)
    ) yield t

    writer.println(s"package $packageText;")
    writer.println()
    writer.println(
      s"""import org.jembi.jempi.libmpi.LibMPI;
         |import org.jembi.jempi.shared.models.CustomEntity;
         |
         |import java.util.List;
         |
         |public final class $custom_className {
         |
         |   private $custom_className() {}
         |
         |   static void updateGoldenRecordFields(final LibMPI libMPI, final String uid) {
         |      final var expandedGoldenRecord = libMPI.getMpiExpandedGoldenRecordList(List.of(uid)).get(0);
         |""".stripMargin)

    muList.zipWithIndex.foreach((mu, _) => {
      val field_name = mu.fieldName
      val fieldName = Utils.snakeCaseToCamelCase(field_name)
      writer.println(
        s"""${" " * 6}BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.$field_name",
           |${" " * 38}expandedGoldenRecord.customGoldenRecord().$fieldName(), CustomEntity::$fieldName);""".stripMargin)
    })
    writer.println()
    config.fields.filter(field => field.isList.isDefined && field.isList.get).foreach(field => {
      val field_name = field.fieldName
      val fieldName = Utils.snakeCaseToCamelCase(field_name)
      writer.println(
        s"""${" " * 6}BackEnd.updateGoldenRecordListField(expandedGoldenRecord, "GoldenRecord.$field_name",
           |${" " * 42}expandedGoldenRecord.entity().$fieldName(),
           |${" " * 42}CustomDocEntity::$fieldName);""".stripMargin)
    })


    writer.println(
      s"""   }
        |
        |}""".stripMargin)
    writer.flush()
    writer.close()
  }

}