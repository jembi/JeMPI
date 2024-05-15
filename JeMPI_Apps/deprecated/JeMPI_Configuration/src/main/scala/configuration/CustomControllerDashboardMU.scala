package configuration

import java.io.{File, PrintWriter}

private object CustomControllerDashboardMU {

  private val classLocation =
    "../JeMPI_Controller/src/main/java/org/jembi/jempi/controller"
  private val customClassName = "CustomControllerDashboardMU"
  private val packageSharedModels = " org.jembi.jempi.controller"

  def generate(config: Config): Unit =

    def fieldParameters(): String =
      config.demographicFields
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 6}MU ${fieldName},"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end fieldParameters

    def fromFields(): String =
      config.demographicFields
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 45}getMU(customFieldTallies.${fieldName}()),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end fromFields

    val classFile: String =
      classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageSharedModels;
         |
         |import org.jembi.jempi.shared.models.CustomFieldTallies;
         |import org.jembi.jempi.shared.models.CustomFieldTallies.FieldTally;
         |
         |record $customClassName(
         |      ${fieldParameters()}) {
         |
         |   static MU getMU(final FieldTally fieldTally) {
         |      if (fieldTally.a() + fieldTally.b() == 0 || fieldTally.c() + fieldTally.d() == 0) {
         |         return new MU(-1.0, -1.0);
         |      }
         |      return new MU(fieldTally.a().doubleValue() / (fieldTally.a().doubleValue() + fieldTally.b().doubleValue()),
         |                    fieldTally.c().doubleValue() / (fieldTally.c().doubleValue() + fieldTally.d().doubleValue()));
         |   }
         |
         |   record MU(
         |      Double m,
         |      Double u) {
         |   }
         |
         |   static CustomControllerDashboardMU fromCustomFieldTallies(final CustomFieldTallies customFieldTallies) {
         |      return new CustomControllerDashboardMU(${fromFields()});
         |   }
         |
         |}
         |""".stripMargin
    )
    writer.flush()
    writer.close()
  end generate

}
