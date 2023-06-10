package configuration

import java.io.{File, PrintWriter}

private object CustomPostgresqlInteraction {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/postgresql"
  private val packageText = "org.jembi.jempi.libmpi.postgresql"
  private val customClassName = "CustomInteractionData"
  private val classFile: String = classLocation + File.separator + customClassName + ".java"
  private val indent = 3


  def generate(fields: Array[DemographicField]): Unit = {
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |
         |final class $customClassName extends CustomDemographicData implements NodeData {
         |
         |   $customClassName(final CustomDemographicData customDemographicData) {
         |      super(${fields.map(field => s"""customDemographicData.${Utils.snakeCaseToCamelCase(field.fieldName)}""").mkString(",\n            ")});
         |   }
         |
         |}
         |""".stripMargin)
    writer.close()
  }

}
