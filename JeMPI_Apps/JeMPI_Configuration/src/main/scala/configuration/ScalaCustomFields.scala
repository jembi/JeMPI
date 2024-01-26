package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object ScalaCustomFields {

  private val classLocation =
    "../JeMPI_EM_Scala/src/main/scala/org/jembi/jempi/em"
  private val custom_className = "CustomFields"
  private val packageText = "org.jembi.jempi.em"

  def generate(config: Config): Any = {
    val classFile: String =
      classLocation + File.separator + custom_className + ".scala"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    writer.println(s"""package $packageText
      |
      |import scala.collection.immutable.ArraySeq
      |
      |object CustomFields {
      |
      |  val FIELDS: ArraySeq[Field] = ArraySeq(
      |    Field("Given Name", 0),
      |    Field("Family Name", 1),
      |    Field("Gender", 2),
      |    Field("Date of Birth", 3),
      |    Field("City", 4),
      |    Field("Mobile", 5),
      |    Field("National ID", 6)
      |  )
      |
      |}
      |""".stripMargin)
    writer.flush()
    writer.close()
  }

}
