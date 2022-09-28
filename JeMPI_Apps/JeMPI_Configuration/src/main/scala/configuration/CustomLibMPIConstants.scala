package org.jembi.jempi
package configuration

import java.io.{File, PrintWriter}

private object CustomLibMPIConstants {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val customClassName = "CustomLibMPIConstants"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |public final class $customClassName {
         |
         |   private $customClassName() {}
         |""".stripMargin)

    // PREDICATE_GOLDEN_RECORD_XXX
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val name = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_GOLDEN_RECORD_${name.toUpperCase} = "GoldenRecord.$name";"""
            .stripMargin)
    }
    writer.println(
      s"""   public static final String PREDICATE_GOLDEN_RECORD_ENTITY_LIST = "GoldenRecord.entity_list";
         |""".stripMargin)

    // PREDICATE_DOCUMENT_FIELDS
    fields.zipWithIndex.foreach {
      case (field, _) =>
        val fieldName = Utils.camelCaseToSnakeCase(field.fieldName)
        writer.println(
          s"""   public static final String PREDICATE_ENTITY_${fieldName.toUpperCase} = "Entity.$fieldName";""")
    }
    writer.println()

    // QUERY_GET_GOLDEN_RECORD_BY_UID
    writer.println(
      s"""   static final String QUERY_GET_GOLDEN_RECORD_BY_UID =
         |      \"\"\"
         |      query goldenRecordByUid($$uid: string) {
         |         all(func: uid($$uid)) {
         |            uid
         |            GoldenRecord.source_id {
         |               uid
         |            }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            GoldenRecord.$name")
    })
    writer.println(
      s"""         }
         |      }
         |      \"\"\";
         |""".stripMargin)

    // QUERY_GET_GOLDEN_RECORD_DOCUMENTS
    writer.println(
      s"""   static final String QUERY_GET_GOLDEN_RECORD_ENTITIES =
         |      \"\"\"
         |      query expandedGoldenRecord() {
         |         all(func: uid(%s)) {
         |            uid
         |            GoldenRecord.source_id {
         |               uid
         |            }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            GoldenRecord.$name")
    })
    writer.println(
      s"""            GoldenRecord.entity_list @facets(score) {
         |               uid
         |               Entity.source_id {
         |                 uid
         |                 SourceId.facility
         |                 SourceId.patient
         |               }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"               Entity.$name")
    })
    writer.println(
      s"""            }
         |         }
         |      }
         |      \"\"\";
         |""".stripMargin)

    // QUERY_GET_ENTITY_BY_UID
    writer.println(
      s"""   static final String QUERY_GET_ENTITY_BY_UID =
         |      \"\"\"
         |      query entityByUid($$uid: string) {
         |         all(func: uid($$uid)) {
         |            uid
         |            Entity.source_id {
         |              uid
         |              SourceId.facility
         |              SourceId.patient
         |            }""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"            Entity.$name")
    })
    writer.println(
      s"""         }
         |      }
         |      \"\"\";
         |""".stripMargin)

    // MUTATION_CREATE_SOURCE_ID_TYPE
    writer.println(
      s"""   static final String MUTATION_CREATE_SOURCE_ID_TYPE =
         |      \"\"\"

         |      type SourceId {
         |         SourceId.facility
         |         SourceId.patient
         |      }
         |      \"\"\";
         """.stripMargin)

    // MUTATION_CREATE_GOLDEN_RECORD_TYPE
    writer.println(
      s"""   static final String MUTATION_CREATE_GOLDEN_RECORD_TYPE =
         |      \"\"\"
         |
         |      type GoldenRecord {
         |         GoldenRecord.source_id:                 [SourceId]""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         GoldenRecord.$name")
    })
    writer.println(
      s"""         GoldenRecord.entity_list:               [Entity]
         |         <~Entity.golden_record_list>
         |      }
         |      \"\"\";
         """.stripMargin)

    // MUTATION_CREATE_DOCUMENT_TYPE
    writer.println(
      s"""   static final String MUTATION_CREATE_ENTITY_TYPE =
         |      \"\"\"
         |
         |      type Entity {
         |         Entity.source_id:                     SourceId""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      writer.println(s"         Entity.$name")
    })
    writer.println(
      s"""         Entity.golden_record_list:            [GoldenRecord]
         |      }
         |      \"\"\";
         |""".stripMargin)

    // MUTATION_CREATE_SOURCE_ID_FIELDS
    writer.println(
      s"""   static final String MUTATION_CREATE_SOURCE_ID_FIELDS =
         |      \"\"\"
         |      SourceId.facility:                     string    @index(exact)                      .
         |      SourceId.patient:                      string    @index(exact)                      .
         |      \"\"\";
         """.stripMargin)

    // MUTATION_CREATE_GOLDEN_RECORD_FIELDS
    writer.println(
      s"""   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
         |      \"\"\"
         |      GoldenRecord.source_id:                [uid]                                        .""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      val index = field.indexGoldenRecord.getOrElse("")
      val fieldType = (if field.isList.isDefined && field.isList.get then "[" else "") +
        field.fieldType.toLowerCase +
        (if field.isList.isDefined && field.isList.get then "]" else "")

      writer.println(
        s"""${" " * 6}GoldenRecord.$name:${" " * (25 - name.length)}$fieldType${
          " " * (10 - fieldType.length)
        }$index${" " * (35 - index.length)}.""".stripMargin)
    })
    writer.println(
      s"""      GoldenRecord.entity_list:              [uid]     @reverse                           .
         |      \"\"\";
         |""".stripMargin)

    // MUTATION_CREATE_DOCUMENT_FIELDS
    writer.println(
      s"""   static final String MUTATION_CREATE_ENTITY_FIELDS =
         |      \"\"\"
         |      Entity.source_id:                    uid                                          .""".stripMargin)
    fields.foreach(field => {
      val name = field.fieldName
      val index = field.indexEntity.getOrElse("")
      val fieldType = field.fieldType.toLowerCase
      writer.println(
        s"""${" " * 6}Entity.$name:${
          " " * (29 - name.length)
        }$fieldType${
          " " * (10 - fieldType.length)
        }$index${
          " " * (35 - index.length)
        }.""".stripMargin)
    })
    writer.println(
      s"""      Entity.golden_record_list:           [uid]     @reverse                           .
         |      \"\"\";
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()
  end generate

}
