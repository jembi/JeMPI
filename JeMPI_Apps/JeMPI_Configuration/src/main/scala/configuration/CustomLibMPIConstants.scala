package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIConstants {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIConstants"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  private def golden_record_predicates(writer: PrintWriter, fields: Array[Field]): Unit = {
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_GOLDEN_RECORD_${name.toUpperCase} = "GoldenRecord.$name";"""
            .stripMargin)
    }
    writer.println(
      s"""   public static final String PREDICATE_GOLDEN_RECORD_PATIENTS = "GoldenRecord.patients";""".stripMargin)
  }

  private def patient_record_predicates(writer: PrintWriter, fields: Array[Field]): Unit = {
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val fieldName = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_PATIENT_RECORD${fieldName.toUpperCase} = "PatientRecord.$fieldName";""".stripMargin)
    }
  }

  private def golden_record_field_names(writer: PrintWriter, fields: Array[Field]): Unit = {
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

    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         GoldenRecord.$name")
    })

    writer.println(
      s"""         \"\"\";""".stripMargin
    )

  }

  private def expanded_golden_record_field_names(writer: PrintWriter, fields: Array[Field]): Unit = {
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

    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         GoldenRecord.$name")
    })

    writer.println(
      s"""         GoldenRecord.patients @facets(score) {
         |            uid
         |            PatientRecord.source_id {
         |               uid
         |               SourceId.facility
         |               SourceId.patient
         |            }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            PatientRecord.$name")
    })
    writer.println(
      s"""         }
         |         \"\"\";""".stripMargin)
  }

  private def patient_record_field_names(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String PATIENT_RECORD_FIELD_NAMES =
         |         \"\"\"
         |         uid
         |         PatientRecord.source_id {
         |            uid
         |            SourceId.facility
         |            SourceId.patient
         |         }""".stripMargin
    )

    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         PatientRecord.$name")
    })

    writer.println(
      s"""         \"\"\";""".stripMargin
    )

  }

  private def expanded_patient_record_field_names(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String EXPANDED_PATIENT_RECORD_FIELD_NAMES =
         |         \"\"\"
         |         uid
         |         PatientRecord.source_id {
         |            uid
         |            SourceId.facility
         |            SourceId.patient
         |         }""".stripMargin
    )

    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         PatientRecord.$name")
    })

    writer.println(
      s"""         ~GoldenRecord.patients @facets(score) {
         |            uid
         |            GoldenRecord.source_id {
         |              uid
         |              SourceId.facility
         |              SourceId.patient
         |            }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            GoldenRecord.$name")
    })
    writer.println(
      s"""         }
         |         \"\"\";
         |""".stripMargin)

  }

  private def query_get_golden_record_by_uid(writer: PrintWriter, fields: Array[Field]): Unit = {
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
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               GoldenRecord.$name")
    })
    writer.println(
      s"""            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def query_get_expanded_golden_records(writer: PrintWriter, fields: Array[Field]): Unit = {
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
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               GoldenRecord.$name")
    })
    writer.println(
      s"""               GoldenRecord.patients @facets(score) {
         |                  uid
         |                  PatientRecord.source_id {
         |                    uid
         |                    SourceId.facility
         |                    SourceId.patient
         |                  }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"                  PatientRecord.$name")
    })
    writer.println(
      s"""               }
         |            }
         |         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def query_get_expanded_patients(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String QUERY_GET_EXPANDED_PATIENTS =
         |         \"\"\"
         |         query expandedPatient() {
         |            all(func: uid(%s)) {
         |               uid
         |               PatientRecord.source_id {
         |                  uid
         |                  SourceId.facility
         |                  SourceId.patient
         |               }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               PatientRecord.$name")
    })
    writer.println(
      s"""               ~GoldenRecord.patients @facets(score) {
         |                  uid
         |                  GoldenRecord.source_id {
         |                    uid
         |                    SourceId.facility
         |                    SourceId.patient
         |                  }""".stripMargin)
    fields.foreach(field => {
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

  private def query_get_patient_by_uid(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String QUERY_GET_PATIENT_BY_UID =
         |         \"\"\"
         |         query patientByUid($$uid: string) {
         |            all(func: uid($$uid)) {
         |               uid
         |               PatientRecord.source_id {
         |                 uid
         |                 SourceId.facility
         |                 SourceId.patient
         |               }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               PatientRecord.$name")
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

  private def mutation_create_source_id_fields(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_SOURCE_ID_FIELDS =
         |         \"\"\"
         |         SourceId.facility:                     string    @index(exact)                      .
         |         SourceId.patient:                      string    @index(exact)                      .
         |         \"\"\";
       """.stripMargin)
  }

  private def mutation_create_golden_record_type(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_GOLDEN_RECORD_TYPE =
         |         \"\"\"
         |
         |         type GoldenRecord {
         |            GoldenRecord.source_id:                 [SourceId]""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            GoldenRecord.$name")
    })
    writer.println(
      s"""            GoldenRecord.patients:                  [PatientRecord]
         |         }
         |         \"\"\";
         """.stripMargin)
  }

  private def mutation_create_golden_record_fields(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
         |         \"\"\"
         |         GoldenRecord.source_id:                [uid]                                        .""".stripMargin)
    fields.foreach(field => {
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
      s"""         GoldenRecord.patients:                 [uid]     @reverse                           .
         |         \"\"\";
         |""".stripMargin)
  }

  private def mutation_create_patient_type(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_PATIENT_TYPE =
         |         \"\"\"
         |
         |         type PatientRecord {
         |            PatientRecord.source_id:                     SourceId""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            PatientRecord.$name")
    })
    writer.println(
      s"""         }
         |         \"\"\";
         |""".stripMargin)
  }

  private def mutation_create_patient_fields(writer: PrintWriter, fields: Array[Field]): Unit = {
    writer.println(
      s"""   static final String MUTATION_CREATE_PATIENT_FIELDS =
         |         \"\"\"
         |         PatientRecord.source_id:                    uid                                          .""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      val index = field.indexEntity.getOrElse("")
      val fieldType = field.fieldType.toLowerCase
      writer.println(
        s"""${" " * 9}PatientRecord.$name:${
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

  def generate(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |public final class $customClassName {
         |""".stripMargin)

    golden_record_predicates(writer, fields)
    patient_record_predicates(writer, fields)
    golden_record_field_names(writer, fields)
    expanded_golden_record_field_names(writer, fields)
    patient_record_field_names(writer, fields)
    expanded_patient_record_field_names(writer, fields)
    query_get_patient_by_uid(writer, fields)
    query_get_golden_record_by_uid(writer, fields)
    query_get_expanded_patients(writer, fields)
    query_get_expanded_golden_records(writer, fields)
    mutation_create_source_id_type(writer)
    mutation_create_source_id_fields(writer, fields)
    mutation_create_golden_record_type(writer, fields)
    mutation_create_golden_record_fields(writer, fields)
    mutation_create_patient_type(writer, fields)
    mutation_create_patient_fields(writer, fields)
    writer.println(
      s"""   private $customClassName() {}
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
