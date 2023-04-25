package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerBackEnd {

  private val classLocation = "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker"
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
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |
         |import java.util.List;
         |
         |public final class $custom_className {
         |
         |   private $custom_className() {
         |   }
         |
         |   static void updateGoldenRecordFields(
         |         final BackEnd backEnd,
         |         final LibMPI libMPI,
         |         final String goldenId) {
         |      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).get(0);
         |      final var goldenRecord = expandedGoldenRecord.goldenRecord();
         |      final var demographicData = goldenRecord.demographicData();
         |      var k = 0;
         |""".stripMargin)

    muList.zipWithIndex.foreach((mu, _) => {
      val field_name = mu.fieldName
      val fieldName = Utils.snakeCaseToCamelCase(field_name)
      writer.println(
        s"""${" " * 6}k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
           |${" " * 6}                                     "$fieldName", demographicData.$fieldName(), CustomDemographicData::$fieldName)
           |${" " * 12}? 1
           |${" " * 12}: 0;""".stripMargin)
    })
    writer.println(
      s"""
         |${" " * 6}if (k > 0) {
         |${" " * 6}  backEnd.updateMatchingPatientRecordScoreForGoldenRecord(expandedGoldenRecord);
         |${" " * 6}}""".stripMargin)
    writer.println()
    config.fields.filter(field => field.isList.isDefined && field.isList.get).foreach(field => {
      val field_name = field.fieldName
      val fieldName = Utils.snakeCaseToCamelCase(field_name)
      writer.println(
        s"""${" " * 6}backEnd.updateGoldenRecordListField(expandedGoldenRecord, "GoldenRecord.$field_name",
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
