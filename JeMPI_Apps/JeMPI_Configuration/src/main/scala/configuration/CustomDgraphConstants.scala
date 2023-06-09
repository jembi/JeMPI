package configuration

import java.io.{File, PrintWriter}

private object CustomDgraphConstants {

  private val classLocation = "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val customClassName = "CustomDgraphConstants"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def golden_record_predicates(writer: PrintWriter, config: Config): Unit = {
    if (config.uniqueGoldenRecordFields.isDefined) {
      config.uniqueGoldenRecordFields.get.zipWithIndex.foreach {
        case (field, _) =>
          val name = Utils.camelCaseToSnakeCase(field.fieldName)
          writer.println(
            s"""   public static final String PREDICATE_GOLDEN_RECORD_${name.toUpperCase} = "GoldenRecord.$name";"""
              .stripMargin)
      }
    }
    config.commonFields.zipWithIndex.foreach {
      case (field, _) =>
        val name = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_GOLDEN_RECORD_${name.toUpperCase} = "GoldenRecord.$name";"""
            .stripMargin)
    }
    writer.println(
      s"""   public static final String PREDICATE_GOLDEN_RECORD_INTERACTIONS = "GoldenRecord.interactions";""".stripMargin)
  }

  private def interaction_predicates(writer: PrintWriter, config: Config): Unit = {
    if (config.uniqueInteractionFields.isDefined) {
      config.uniqueInteractionFields.get.zipWithIndex.foreach {
        case (field, _) =>
          val name = Utils.camelCaseToSnakeCase(field.fieldName)
          writer.println(
            s"""   public static final String PREDICATE_INTERACTION_${name.toUpperCase} = "Interaction.$name";"""
              .stripMargin)
      }
    }
    config.commonFields.zipWithIndex.foreach {
      case (field, _) =>
        val fieldName = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_INTERACTION_${fieldName.toUpperCase} = "Interaction.$fieldName";""".stripMargin)
    }
  }

  private def golden_record_field_names(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""
         |   static final String GOLDEN_RECORD_FIELD_NAMES =
         |         \"\"\"
         |         uid
         |         GoldenRecord.source_id {
         |            uid
         |            SourceId.facility
         |            SourceId.patient
         |         }""".stripMargin
    )
    if (config.uniqueGoldenRecordFields.isDefined) {
      config.uniqueGoldenRecordFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"         GoldenRecord.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         GoldenRecord.$name")
    })
    writer.println(
      s"""         \"\"\";""".stripMargin
    )

  }

  private def expanded_golden_record_field_names(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""
         |   static final String EXPANDED_GOLDEN_RECORD_FIELD_NAMES =
         |         \"\"\"
         |         uid
         |         GoldenRecord.source_id {
         |            uid
         |            SourceId.facility
         |            SourceId.patient
         |         }""".stripMargin
    )

    if (config.uniqueGoldenRecordFields.isDefined) {
      config.uniqueGoldenRecordFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"         GoldenRecord.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         GoldenRecord.$name")
    })
    writer.println(
      s"""         GoldenRecord.interactions @facets(score) {
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
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            Interaction.$name")
    })
    writer.println(
      s"""         }
         |         \"\"\";""".stripMargin)
  }

  private def interaction_field_names(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String INTERACTION_FIELD_NAMES =
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
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         Interaction.$name")
    })

    writer.println(
      s"""         \"\"\";
         |""".stripMargin
    )

  }

  private def expanded_interaction_field_names(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String EXPANDED_INTERACTION_FIELD_NAMES =
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
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         Interaction.$name")
    })

    writer.println(
      s"""         ~GoldenRecord.interactions @facets(score) {
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
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            GoldenRecord.$name")
    })
    writer.println(
      s"""         }
         |         \"\"\";
         |""".stripMargin)

  }

  private def query_get_golden_record_by_uid(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String QUERY_GET_GOLDEN_RECORD_BY_UID =
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
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               GoldenRecord.$name")
    })
    writer.println(
      s"""            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def query_get_golden_records(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String QUERY_GET_GOLDEN_RECORDS =
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
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               GoldenRecord.$name")
    })
    writer.println(
      s"""            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def query_get_expanded_golden_records(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String QUERY_GET_EXPANDED_GOLDEN_RECORDS =
         |         \"\"\"
         |         query expandedGoldenRecord() {
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
    config.commonFields.foreach(field => {
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
         |                  }""".stripMargin)
    if (config.uniqueInteractionFields.isDefined) {
      config.uniqueInteractionFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"                  Interaction.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"                  Interaction.$name")
    })
    writer.println(
      s"""               }
         |            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def query_get_expanded_interactions(writer: PrintWriter, config: Config): Unit = {
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
         |               }""".stripMargin)
    if (config.uniqueInteractionFields.isDefined) {
      config.uniqueInteractionFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"               Interaction.$name")
      })
    }
    config.commonFields.foreach(field => {
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
         |                  }""".stripMargin)
    if (config.uniqueGoldenRecordFields.isDefined) {
      config.uniqueGoldenRecordFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"                  GoldenRecord.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"                  GoldenRecord.$name")
    })
    writer.println(
      s"""               }
         |            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def query_get_interaction_by_uid(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String QUERY_GET_INTERACTION_BY_UID =
         |         \"\"\"
         |         query interactionByUid($$uid: string) {
         |            all(func: uid($$uid)) {
         |               uid
         |               Interaction.source_id {
         |                 uid
         |                 SourceId.facility
         |                 SourceId.patient
         |               }""".stripMargin)
    if (config.uniqueInteractionFields.isDefined) {
      config.uniqueInteractionFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"               Interaction.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               Interaction.$name")
    })
    writer.println(
      s"""            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def mutation_create_source_id_type(writer: PrintWriter): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_SOURCE_ID_TYPE =
         |         \"\"\"
         |         type SourceId {
         |            SourceId.facility
         |            SourceId.patient
         |         }
         |         \"\"\";
     """.stripMargin)
  }

  private def mutation_create_source_id_fields(writer: PrintWriter): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_SOURCE_ID_FIELDS =
         |         \"\"\"
         |         SourceId.facility:                     string    @index(exact)                      .
         |         SourceId.patient:                      string    @index(exact)                      .
         |         \"\"\";
       """.stripMargin)
  }

  private def mutation_create_golden_record_type(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_GOLDEN_RECORD_TYPE =
         |         \"\"\"
         |
         |         type GoldenRecord {
         |            GoldenRecord.source_id:                 [SourceId]""".stripMargin)
    if (config.uniqueGoldenRecordFields.isDefined) {
      config.uniqueGoldenRecordFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"            GoldenRecord.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            GoldenRecord.$name")
    })
    writer.println(
      s"""            GoldenRecord.interactions:              [Interaction]
         |         }
         |         \"\"\";
         """.stripMargin)
  }

  private def mutation_create_golden_record_fields(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
         |         \"\"\"
         |         GoldenRecord.source_id:                [uid]                                        .""".stripMargin)
    if (config.uniqueGoldenRecordFields.isDefined) {
      config.uniqueGoldenRecordFields.get.foreach(field => {
        val name = field.fieldName
        val index = field.index.getOrElse("")
        val fieldType = (if field.isList.isDefined && field.isList.get then "[" else "") +
          field.fieldType.toLowerCase +
          (if field.isList.isDefined && field.isList.get then "]" else "")
        writer.println(
          s"""${" " * 9}GoldenRecord.$name:${" " * (25 - name.length)}$fieldType${
            " " * (10 - fieldType.length)
          }$index${" " * (35 - index.length)}.""".stripMargin)
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      val index = field.indexGoldenRecord.getOrElse("")
      val fieldType = (if field.isList.isDefined && field.isList.get then "[" else "") +
        field.fieldType.toLowerCase +
        (if field.isList.isDefined && field.isList.get then "]" else "")
      writer.println(
        s"""${" " * 9}GoldenRecord.$name:${" " * (25 - name.length)}$fieldType${
          " " * (10 - fieldType.length)
        }$index${" " * (35 - index.length)}.""".stripMargin)
    })
    writer.println(
      s"""         GoldenRecord.interactions:             [uid]     @reverse                           .
         |         \"\"\";
         |""".stripMargin)
  }

  private def mutation_create_interaction_type(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_INTERACTION_TYPE =
         |         \"\"\"
         |
         |         type Interaction {
         |            Interaction.source_id:                     SourceId""".stripMargin)
    if (config.uniqueInteractionFields.isDefined) {
      config.uniqueInteractionFields.get.foreach(field => {
        val name = field.fieldName
        writer.println(s"            Interaction.$name")
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            Interaction.$name")
    })
    writer.println(
      s"""         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def mutation_create_interaction_fields(writer: PrintWriter, config: Config): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_INTERACTION_FIELDS =
         |         \"\"\"
         |         Interaction.source_id:                    uid                                          .""".stripMargin)
    if (config.uniqueInteractionFields.isDefined) {
      config.uniqueInteractionFields.get.foreach(field => {
        val name = field.fieldName
        val index = field.index.getOrElse("")
        val fieldType = (if field.isList.isDefined && field.isList.get then "[" else "") +
          field.fieldType.toLowerCase +
          (if field.isList.isDefined && field.isList.get then "]" else "")
        writer.println(
          s"""${" " * 9}Interaction.$name:${" " * (29 - name.length)}$fieldType${
            " " * (10 - fieldType.length)
          }$index${" " * (35 - index.length)}.""".stripMargin)
      })
    }
    config.commonFields.foreach(field => {
      val name = field.fieldName
      val index = field.indexEntity.getOrElse("")
      val fieldType = field.fieldType.toLowerCase
      writer.println(
        s"""${" " * 9}Interaction.$name:${
          " " * (29 - name.length)
        }$fieldType${
          " " * (10 - fieldType.length)
        }$index${
          " " * (35 - index.length)
        }.""".stripMargin)
    })
    writer.println(
      s"""         \"\"\";
         |""".stripMargin)
  }

  def generate(config: Config): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |public final class $customClassName {
         |""".stripMargin)

    golden_record_predicates(writer, config)
    interaction_predicates(writer, config)
    golden_record_field_names(writer, config)
    expanded_golden_record_field_names(writer, config)
    interaction_field_names(writer, config)
    expanded_interaction_field_names(writer, config)
    query_get_interaction_by_uid(writer, config)
    query_get_golden_record_by_uid(writer, config)
    query_get_expanded_interactions(writer, config)
    query_get_golden_records(writer, config)
    query_get_expanded_golden_records(writer, config)
    mutation_create_source_id_type(writer)
    mutation_create_source_id_fields(writer)
    mutation_create_golden_record_type(writer, config)
    mutation_create_golden_record_fields(writer, config)
    mutation_create_interaction_type(writer, config)
    mutation_create_interaction_fields(writer, config)
    writer.println(
      s"""   private $customClassName() {}
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
