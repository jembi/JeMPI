package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphIndexes {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val custom_className = "CustomDgraphIndexes"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(config: Config): Unit = {
      val classFile: String =
        classLocation + File.separator + custom_className + ".java"
      println("Creating " + classFile)
      val file: File = new File(classFile)
      val writer: PrintWriter = new PrintWriter(file)
      writer.println(
        s"""package $packageText;
           |
           |
           |final class $custom_className {
           |
           |   private $custom_className() {
           |   }
           |
           |    public static Boolean shouldUpdateLinkingIndexes() {
           |        return true;
           |    }
           |""".stripMargin);

      get_default_indexes()
      get_remove_all_indexes()
      get_linking_indexes()

      writer.println(
        s"""
           |}
           |""".stripMargin)

      writer.flush()
      writer.close()


      def get_default_indexes(): Unit = {
        write_out_indexs_fields("LOAD_DEFAULT_INDEXES", write_field_with_index)
      }

      def get_remove_all_indexes(): Unit = {
        write_out_indexs_fields("REMOVE_ALL_INDEXES", write_field_without_index)
      }

      def get_linking_indexes(): Unit = {
        write_out_linking_fields("LOAD_LINKING_INDEXES", "linking")
      }

      def write_out_linking_fields(prop_name: String, index_type:String): Unit = {
        writer.println(
          s"""   static final String $prop_name =
             |         \"\"\"
             |""".stripMargin)

        var indexsObjOption:Option[Map[String, IndexProps]] = null;

        if (index_type == "linking") {
          indexsObjOption = config.indexes.linking
        } else if (index_type == "matching") {
          indexsObjOption = config.indexes.matching
        }

        if (indexsObjOption.isDefined){
          val indexsObj = indexsObjOption.get

          val index_names = indexsObj.keySet
          val valid_index_fields = config.demographicFields.filter(f => index_names.contains(f.fieldName))

          if (valid_index_fields.size != indexsObj.size) {
            throw new IllegalArgumentException("Some fields defined in the index property do not exist. Make sure you have defined them in the demographicFields property")
          }

          valid_index_fields.foreach(field => {
            val indexPropsOpt = indexsObj.get(field.fieldName)

            if (indexPropsOpt.isDefined){
              write_field_with_index(field, "GoldenRecord", indexPropsOpt.get.props, 29)
              write_field_with_index(field, "Interaction", indexPropsOpt.get.props, 30)
            }
          })
        }

        writer.println(
          s"""
             |         \"\"\";
             |""".stripMargin)

      }

      def write_out_indexs_fields(prop_name: String, write_func: (field: DemographicField
                                                                        , recordType: String
                                                                        , index: String
                                                                        , spacing: Int) => Unit): Unit = {
            writer.println(
              s"""   static final String $prop_name =
                 |         \"\"\"
                 |""".stripMargin)

            config.demographicFields
              .foreach(field => {
                val indexGoldenRecord = field.indexGoldenRecord.getOrElse("")
                val indexInteraction = field.indexInteraction.getOrElse("")

                if (indexGoldenRecord != "") {
                  write_func(field, "GoldenRecord", indexGoldenRecord, 29)
                }

                if (indexInteraction != "") {
                  write_func(field, "Interaction", indexInteraction, 30)
                }
              })

            writer.println(
              s"""
                 |         \"\"\";
                 |""".stripMargin)
          }

      def write_field_with_index(field: DemographicField, recordType: String, index: String, spacing: Int): Unit = {
        val name = field.fieldName

        val fieldType =
          (if field.isList.isDefined && field.isList.get then "["
          else "") + field.fieldType.toLowerCase + (if field.isList.isDefined && field.isList.get
            then "]"
          else "")
        writer.println(
          s"""${" " * 9}$recordType.$name:${" " * (spacing - name.length)}$fieldType${" " * (10 - fieldType.length)}$index${" " * (35 - index.length)}.""".stripMargin
        )
      }

      def write_field_without_index(field: DemographicField, recordType: String, index: String, spacing: Int): Unit = {
        val name = field.fieldName

        val fieldType =
          (if field.isList.isDefined && field.isList.get then "["
          else "") + field.fieldType.toLowerCase + (if field.isList.isDefined && field.isList.get
            then "]"
          else "")
        writer.println(
          s"""${" " * 9}$recordType.$name:${" " * (spacing - name.length)}$fieldType${" " * (10 - fieldType.length)}${" " * 35}.""".stripMargin
        )
      }

  }


}
