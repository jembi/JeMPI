package configuration

import java.io.{File, PrintWriter}

private object CustomAsyncHelper {

  private val classLocation =
    "../JeMPI_AsyncReceiver/src/main/java/org/jembi/jempi/async_receiver"
  private val customClassName = "CustomAsyncHelper"
  private val packageText = "org.jembi.jempi.async_receiver"

  def generate(config: Config): Unit =
    val classFile: String =
      classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(s"""package $packageText;
         |
         |import org.apache.commons.csv.CSVRecord;
         |import org.jembi.jempi.shared.models.CustomSourceId;
         |import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
         |
         |final class $customClassName {
         |
         |${columnIndices()}
         |
         |   private $customClassName() {
         |   }
         |
         |   static CustomUniqueInteractionData customUniqueInteractionData(final CSVRecord csvRecord) {
         |      return new CustomUniqueInteractionData(${customUniqueInteractionArguments()});
         |   }
         |
         |${
                       if (config.additionalNodes.isEmpty) ""
                       else
                         config.additionalNodes.get
                           .map(x => customNodeConstructor(x))
                           .mkString
                     }
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()

    def columnIndices(): String =

      def additionalNodeFields(additionalNode: AdditionalNode): String =
        additionalNode.fields
          .map(f =>
            if (f.csvCol.isEmpty) ""
            else
              s"""   private static final int ${additionalNode.nodeName.toUpperCase}_${f.fieldName.toUpperCase}_COL_NUM = ${f.csvCol.get};
               |""".stripMargin
          )
          .mkString("")
          .stripMargin
          .stripTrailing()
      end additionalNodeFields

      def uniqueInteractionFields(fields: Array[UniqueField]): String =
        fields
          .map(f =>
            if (f.csvCol.isEmpty) ""
            else
              s"""   private static final int ${f.fieldName
                  .toUpperCase()}_COL_NUM = ${f.csvCol.get};
               |""".stripMargin
          )
          .mkString("")
          .stripMargin
      end uniqueInteractionFields

      (if (config.uniqueInteractionFields.isEmpty) ""
       else
         uniqueInteractionFields(config.uniqueInteractionFields.get))
        +
          (if (
             config.additionalNodes.isEmpty || config.additionalNodes.get.isEmpty
           ) ""
           else
             config.additionalNodes.get
               .map(x => s"""${additionalNodeFields(x)}""")
               .mkString(sys.props("line.separator"))) + sys.props(
            "line.separator"
          )
          +
          config.demographicFields
            .filter(f => f.source.isDefined && f.source.get.csvCol.isDefined)
            .map(f =>
              s"""${" " * 3}private static final int ${f.fieldName.toUpperCase}_COL_NUM = ${f.source.get.csvCol.get};"""
            )
            .mkString(sys.props("line.separator"))
    end columnIndices

    def demographicFields(): String =
      config.demographicFields
        .map(f =>
          if (f.source.isDefined && f.source.get.generate.isDefined) {
            s"""${" " * 9}null,"""
          } else {
            s"""${" " * 9}csvRecord.get(${f.fieldName.toUpperCase}_COL_NUM),"""
          }
        )
        .mkString(sys.props("line.separator"))
        .dropRight(1)
    end demographicFields

    def customUniqueInteractionArguments(): String =
      if (config.uniqueInteractionFields.isEmpty) ""
      else
        config.uniqueInteractionFields.get
          .map(f =>
            if (f.fieldName.toUpperCase.equals("AUX_ID")) {
              s"""${" " * 45}Main.parseRecordNumber(csvRecord.get(${f.fieldName.toUpperCase}_COL_NUM))"""
            } else if (f.fieldName.toUpperCase.equals("AUX_DATE_CREATED")) {
              s"""${" " * 45}java.time.LocalDateTime.now()"""
            } else {
              s"""${" " * 45}csvRecord.get(${f.fieldName.toUpperCase}_COL_NUM)"""
            }
          )
          .mkString(""",
              |""".stripMargin)
          .trim
    end customUniqueInteractionArguments

    def customNodeConstructor(additionalNode: AdditionalNode): String =

      def arguments(fields: Array[AdditionalNodeField]): String =
        fields
          .map(f =>
            if (f.csvCol.isEmpty)
              s"""${" " * 9}null"""
            else
              s"""${" " * 9}csvRecord.get(${additionalNode.nodeName.toUpperCase}_${f.fieldName
                  .toUpperCase()}_COL_NUM)"""
          )
          .mkString(s",${sys.props("line.separator")}")
      end arguments

      s"""   static Custom${additionalNode.nodeName} custom${additionalNode.nodeName}(final CSVRecord csvRecord) {
         |      return new Custom${additionalNode.nodeName}(
         |         null,
         |${arguments(additionalNode.fields)});
         |   }
         |""".stripMargin
    end customNodeConstructor

  end generate

}
