package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphConstants {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphConstants"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(config: Config): Unit = {

    val classFile: String =
      classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    writer.println(s"""package $packageText;
         |
         |public final class $customClassName {
         |""".stripMargin)

    golden_record_predicates()
    interaction_predicates()
    golden_record_field_names()
    expanded_golden_record_field_names()
    interaction_field_names()
    expanded_interaction_field_names()
    query_get_interaction_by_uid()
    query_get_golden_record_by_uid()
    query_get_expanded_interactions()
    query_get_golden_records()
    query_get_expanded_golden_records()
    mutation_create_source_id_type()
    mutation_create_source_id_fields()
    mutation_create_golden_record_type()
    mutation_create_golden_record_fields()
    mutation_create_interaction_type()
    mutation_create_interaction_fields()

    writer.println(s"""   private $customClassName() {}
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()

    def golden_record_predicates(): Unit =
      if (config.uniqueGoldenRecordFields.isDefined)
        config.uniqueGoldenRecordFields.get.zipWithIndex.foreach {
          case (field, _) =>
            val name = Utils.camelCaseToSnakeCase(field.fieldName)
            writer.println(
              s"""   public static final String PREDICATE_GOLDEN_RECORD_${name.toUpperCase} = "GoldenRecord.$name";""".stripMargin
            )
        }
      end if
      config.demographicFields.zipWithIndex.foreach { case (field, _) =>
        val name = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_GOLDEN_RECORD_${name.toUpperCase} = "GoldenRecord.$name";""".stripMargin
        )
      }
      writer.println(
        s"""   public static final String PREDICATE_GOLDEN_RECORD_INTERACTIONS = "GoldenRecord.interactions";""".stripMargin
      )
    end golden_record_predicates

    def interaction_predicates(): Unit =
      if (config.uniqueInteractionFields.isDefined)
        config.uniqueInteractionFields.get.zipWithIndex.foreach {
          case (field, _) =>
            val name = Utils.camelCaseToSnakeCase(field.fieldName)
            writer.println(
              s"""   public static final String PREDICATE_INTERACTION_${name.toUpperCase} = "Interaction.$name";""".stripMargin
            )
        }
      end if
      config.demographicFields.zipWithIndex.foreach { case (field, _) =>
        val fieldName = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_INTERACTION_${fieldName.toUpperCase} = "Interaction.$fieldName";""".stripMargin
        )
      }
    end interaction_predicates

    def golden_record_field_names(): Unit =
      writer.println(s"""
           |   static final String DEPRECATED_GOLDEN_RECORD_FIELD_NAMES =
           |         \"\"\"
           |         uid
           |         GoldenRecord.source_id {
           |            uid
           |            SourceId.facility
           |            SourceId.patient
           |         }""".stripMargin)
      if (config.uniqueGoldenRecordFields.isDefined)
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"         GoldenRecord.$name")
        })
      end if
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"         GoldenRecord.$name")
      })
      writer.println(s"""         \"\"\";""".stripMargin)
    end golden_record_field_names

    def expanded_golden_record_field_names(): Unit = {
      writer.println(s"""
           |   static final String DEPRECATED_EXPANDED_GOLDEN_RECORD_FIELD_NAMES =
           |         \"\"\"
           |         uid
           |         GoldenRecord.source_id {
           |            uid
           |            SourceId.facility
           |            SourceId.patient
           |         }""".stripMargin)

      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"         GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"         GoldenRecord.$name")
      })
      writer.println(s"""         GoldenRecord.interactions @facets(score) {
           |            uid
           |            Interaction.source_id {
           |               uid
           |               SourceId.facility
           |               SourceId.patient
           |            }""".stripMargin)
      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"            Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"            Interaction.$name")
      })
      writer.println(s"""         }
           |         \"\"\";""".stripMargin)
    }

    def interaction_field_names(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_INTERACTION_FIELD_NAMES =
           |         \"\"\"
           |         uid
           |         Interaction.source_id {
           |            uid
           |            SourceId.facility
           |            SourceId.patient
           |         }""".stripMargin
      )

      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"         Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"         Interaction.$name")
      })

      writer.println(s"""         \"\"\";
           |""".stripMargin)

    }

    def expanded_interaction_field_names(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_EXPANDED_INTERACTION_FIELD_NAMES =
           |         \"\"\"
           |         uid
           |         Interaction.source_id {
           |            uid
           |            SourceId.facility
           |            SourceId.patient
           |         }""".stripMargin
      )

      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"         Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"         Interaction.$name")
      })

      writer.println(s"""         ~GoldenRecord.interactions @facets(score) {
           |            uid
           |            GoldenRecord.source_id {
           |              uid
           |              SourceId.facility
           |              SourceId.patient
           |            }""".stripMargin)

      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"            GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"            GoldenRecord.$name")
      })
      writer.println(s"""         }
           |         \"\"\";
           |""".stripMargin)

    }

    def query_get_interaction_by_uid(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_QUERY_GET_INTERACTION_BY_UID =
           |         \"\"\"
           |         query interactionByUid($$uid: string) {
           |            all(func: uid($$uid)) {
           |               uid
           |               Interaction.source_id {
           |                 uid
           |                 SourceId.facility
           |                 SourceId.patient
           |               }""".stripMargin
      )
      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"               Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"               Interaction.$name")
      })
      writer.println(s"""            }
           |         }
           |         \"\"\";
           |""".stripMargin)
    }

    def query_get_golden_record_by_uid(): Unit = {
      writer.println(s"""   static final String QUERY_GET_GOLDEN_RECORD_BY_UID =
           |         \"\"\"
           |         query goldenRecordByUid($$uid: string) {
           |            all(func: uid($$uid)) {
           |               uid
           |               GoldenRecord.source_id {
           |                  uid
           |                  SourceId.facility
           |                  SourceId.patient
           |               }""".stripMargin)
      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"               GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"               GoldenRecord.$name")
      })
      writer.println(s"""            }
           |         }
           |         \"\"\";
           |""".stripMargin)
    }

    def query_get_expanded_interactions(): Unit = {
      writer.println(
        s"""   static final String QUERY_GET_EXPANDED_INTERACTIONS =
           |         \"\"\"
           |         query expandedInteraction() {
           |            all(func: uid(%s)) {
           |               uid
           |               Interaction.source_id {
           |                  uid
           |                  SourceId.facility
           |                  SourceId.patient
           |               }""".stripMargin
      )
      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"               Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"               Interaction.$name")
      })
      writer.println(
        s"""               ~GoldenRecord.interactions @facets(score) {
           |                  uid
           |                  GoldenRecord.source_id {
           |                    uid
           |                    SourceId.facility
           |                    SourceId.patient
           |                  }""".stripMargin
      )
      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"                  GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"                  GoldenRecord.$name")
      })
      writer.println(s"""               }
           |            }
           |         }
           |         \"\"\";
           |""".stripMargin)
    }

    def query_get_golden_records(): Unit = {
      writer.println(s"""   static final String QUERY_GET_GOLDEN_RECORDS =
           |         \"\"\"
           |         query goldenRecord() {
           |            all(func: uid(%s)) {
           |               uid
           |               GoldenRecord.source_id {
           |                  uid
           |                  SourceId.facility
           |                  SourceId.patient
           |               }""".stripMargin)
      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"               GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"               GoldenRecord.$name")
      })
      writer.println(s"""            }
           |         }
           |         \"\"\";
           |""".stripMargin)
    }

    def query_get_expanded_golden_records(): Unit = {
      writer.println(s"""   static final String QUERY_GET_EXPANDED_GOLDEN_RECORDS =
           |         \"\"\"
           |         query expandedGoldenRecord() {
           |            all(func: uid(%s), orderdesc: GoldenRecord.aux_date_created) {
           |               uid
           |               GoldenRecord.source_id {
           |                  uid
           |                  SourceId.facility
           |                  SourceId.patient
           |               }""".stripMargin)
      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"               GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"               GoldenRecord.$name")
      })
      writer.println(
        s"""               GoldenRecord.interactions @facets(score) {
           |                  uid
           |                  Interaction.source_id {
           |                    uid
           |                    SourceId.facility
           |                    SourceId.patient
           |                  }""".stripMargin
      )
      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"                  Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"                  Interaction.$name")
      })
      writer.println(s"""               }
           |            }
           |         }
           |         \"\"\";
           |""".stripMargin)
    }

    def mutation_create_source_id_type(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_MUTATION_CREATE_SOURCE_ID_TYPE =
           |         \"\"\"
           |         type SourceId {
           |            SourceId.facility
           |            SourceId.patient
           |         }
           |         \"\"\";
       """.stripMargin
      )
    }

    def mutation_create_source_id_fields(): Unit = {
      writer.println(s"""   static final String DEPRECATED_MUTATION_CREATE_SOURCE_ID_FIELDS =
           |         \"\"\"
           |         SourceId.facility:                     string    @index(exact)                      .
           |         SourceId.patient:                      string    @index(exact)                      .
           |         \"\"\";
         """.stripMargin)
    }

    def mutation_create_golden_record_type(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_MUTATION_CREATE_GOLDEN_RECORD_TYPE =
           |         \"\"\"
           |
           |         type GoldenRecord {
           |            GoldenRecord.source_id:                 [SourceId]""".stripMargin
      )
      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"            GoldenRecord.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"            GoldenRecord.$name")
      })
      writer.println(
        s"""            GoldenRecord.interactions:              [Interaction]
           |         }
           |         \"\"\";
           """.stripMargin
      )
    }

    def mutation_create_golden_record_fields(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
           |         \"\"\"
           |         GoldenRecord.source_id:                [uid]     @reverse                           .""".stripMargin
      )
      if (config.uniqueGoldenRecordFields.isDefined) {
        config.uniqueGoldenRecordFields.get.foreach(field => {
          val name = field.fieldName
          val index = field.index.getOrElse("")
          val fieldType =
            (if field.isList.isDefined && field.isList.get then "["
             else "") + field.fieldType.toLowerCase + (if field.isList.isDefined && field.isList.get
                                                       then "]"
                                                       else "")
          writer.println(
            s"""${" " * 9}GoldenRecord.$name:${" " * (25 - name.length)}$fieldType${" " * (10 - fieldType.length)}$index${" " * (35 - index.length)}.""".stripMargin
          )
        })
      }
      config.demographicFields
        .foreach(field => {
          val name = field.fieldName
          val index = field.indexGoldenRecord.getOrElse("")
          val fieldType =
            (if field.isList.isDefined && field.isList.get then "["
             else "") + field.fieldType.toLowerCase + (if field.isList.isDefined && field.isList.get
                                                       then "]"
                                                       else "")
          writer.println(
            s"""${" " * 9}GoldenRecord.$name:${" " * (25 - name.length)}$fieldType${" " * (10 - fieldType.length)}$index${" " * (35 - index.length)}.""".stripMargin
          )
        })
      writer.println(
        s"""         GoldenRecord.interactions:             [uid]     @reverse                           .
           |         \"\"\";
           |""".stripMargin
      )
    }

    def mutation_create_interaction_type(): Unit =
      writer.println(
        s"""   static final String DEPRECATED_MUTATION_CREATE_INTERACTION_TYPE =
           |         \"\"\"
           |
           |         type Interaction {
           |            Interaction.source_id:                     SourceId""".stripMargin
      )
      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          writer.println(s"            Interaction.$name")
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        writer.println(s"            Interaction.$name")
      })
      writer.println(s"""         }
           |         \"\"\";
           |""".stripMargin)
    end mutation_create_interaction_type

    def mutation_create_interaction_fields(): Unit = {
      writer.println(
        s"""   static final String DEPRECATED_MUTATION_CREATE_INTERACTION_FIELDS =
           |         \"\"\"
           |         Interaction.source_id:                    uid                                          .""".stripMargin
      )
      if (config.uniqueInteractionFields.isDefined) {
        config.uniqueInteractionFields.get.foreach(field => {
          val name = field.fieldName
          val index = field.index.getOrElse("")
          val fieldType =
            (if field.isList.isDefined && field.isList.get then "["
             else "") + field.fieldType.toLowerCase + (if field.isList.isDefined && field.isList.get
                                                       then "]"
                                                       else "")
          writer.println(
            s"""${" " * 9}Interaction.$name:${" " * (29 - name.length)}$fieldType${" " * (10 - fieldType.length)}$index${" " * (35 - index.length)}.""".stripMargin
          )
        })
      }
      config.demographicFields.foreach(field => {
        val name = field.fieldName
        val index = field.indexInteraction.getOrElse("")
        val fieldType = field.fieldType.toLowerCase
        writer.println(
          s"""${" " * 9}Interaction.$name:${" " * (29 - name.length)}$fieldType${" " * (10 - fieldType.length)}$index${" " * (35 - index.length)}.""".stripMargin
        )
      })
      writer.println(s"""         \"\"\";
           |""".stripMargin)
    }

  }
  end generate

}
