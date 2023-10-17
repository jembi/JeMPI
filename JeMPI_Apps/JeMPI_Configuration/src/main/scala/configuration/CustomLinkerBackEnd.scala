package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerBackEnd {

  private val classLocation = "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker/backend"
  private val custom_className = "CustomLinkerBackEnd"
  private val packageText = "org.jembi.jempi.linker.backend"

  def generate(config: Config): Any = {

    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    def createGenerateFunctions(): String = {
      config.demographicFields
        .filter(f => f.source.isDefined && f.source.get.generate.isDefined)
        .map(f => s"""   public static final Supplier<String> GENERATE_${Utils.camelCaseToSnakeCase(Utils.snakeCaseToCamelCase(f.fieldName)).toUpperCase} = ${f.source.get.generate.get.func};""".stripMargin)
        .mkString("\n")
        .trim
    }

    def createApplyFunctions() : String = {

      def applyFields() : String = {

        def applyFunction(f : DemographicField) : String = {
          if (f.source.isDefined && f.source.get.generate.isDefined) {
            s"""GENERATE_${Utils.camelCaseToSnakeCase(Utils.snakeCaseToCamelCase(f.fieldName)).toUpperCase()}.get()""".stripMargin
          }  else {
            s"""interaction.demographicData().${Utils.snakeCaseToCamelCase(f.fieldName)}""".stripMargin
          }
        }

        config.demographicFields.map(f => s"""${" " * 55}${applyFunction(f)},""".stripMargin)
          .mkString("\n")
          .drop(55)  // drop 55 spaces
          .dropRight(1);  // drop the comma

      }

      s"""public static Interaction applyAutoCreateFunctions(final Interaction interaction) {
         |${" " * 3}   return new Interaction(interaction.interactionId(),
         |${" " * 3}                          interaction.sourceId(),
         |${" " * 3}                          interaction.uniqueInteractionData(),
         |${" " * 3}                          new CustomDemographicData(${applyFields()}));
         |${" " * 3}}""".stripMargin
    }

    config.demographicFields
      .filter(f => f.linkMetaData.isDefined)
      .foreach(f => {
        var t = (f.fieldName, f.linkMetaData.get.m, f.linkMetaData.get.u)
      })
    val muList = for (
      t <- config.demographicFields.filter(f => f.linkMetaData.isDefined)
    ) yield t

    writer.println(s"package $packageText;")
    writer.println()
    writer.println(
      s"""import org.jembi.jempi.libmpi.LibMPI;
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |import org.jembi.jempi.shared.models.Interaction;
         |import org.jembi.jempi.shared.utils.AppUtils;
         |
         |import java.util.List;
         |import java.util.function.Supplier;
         |
         |public final class $custom_className {
         |
         |   private $custom_className() {
         |   }
         |
         |   ${createGenerateFunctions()}
         |
         |   ${createApplyFunctions()}
         |
         |   static void updateGoldenRecordFields(
         |         final LibMPI libMPI,
         |         final float threshold,
         |         final String interactionId,
         |         final String goldenId) {
         |      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).get(0);
         |      final var goldenRecord = expandedGoldenRecord.goldenRecord();
         |      final var demographicData = goldenRecord.demographicData();
         |      var k = 0;
         |""".stripMargin)

    config.demographicFields.foreach(f => {
      val field_name = f.fieldName
      val fieldName = Utils.snakeCaseToCamelCase(field_name)
      writer.println(
        s"""${" " * 6}k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
           |${" " * 6}                                            "$fieldName", demographicData.$fieldName, CustomDemographicData::get${fieldName.charAt(0).toUpper}${fieldName.substring(1)})
           |${" " * 12}? 1
           |${" " * 12}: 0;""".stripMargin)
    })
    writer.println(
      s"""
         |${" " * 6}if (k > 0) {
         |${" " * 6}  LinkerDWH.helperUpdateInteractionsScore(libMPI, threshold, expandedGoldenRecord);
         |${" " * 6}}""".stripMargin)
    writer.println()
    config.demographicFields.filter(field => field.isList.isDefined && field.isList.get).foreach(field => {
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
