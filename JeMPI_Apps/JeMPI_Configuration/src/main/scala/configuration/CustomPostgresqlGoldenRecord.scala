package configuration

import java.io.{File, PrintWriter}

private object CustomPostgresqlGoldenRecord {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/postgresql"
  private val packageText = "org.jembi.jempi.libmpi.postgresql"
  private val customClassName = "CustomGoldenRecordData"
  private val classFile: String =
    classLocation + File.separator + customClassName + ".java"

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
         |      super(customDemographicData);
         |   }
         |
         |}""".stripMargin
    )
    writer.close()
  }

}
