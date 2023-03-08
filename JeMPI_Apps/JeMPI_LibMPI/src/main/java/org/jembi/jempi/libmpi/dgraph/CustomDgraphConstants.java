package org.jembi.jempi.libmpi.dgraph;

public final class CustomDgraphConstants {

   public static final String PREDICATE_GOLDEN_RECORD_AUX_ID = "GoldenRecord.aux_id";
   public static final String PREDICATE_GOLDEN_RECORD_FPID = "GoldenRecord.fpid";
   public static final String PREDICATE_GOLDEN_RECORD_GENDER = "GoldenRecord.gender";
   public static final String PREDICATE_GOLDEN_RECORD_DOB = "GoldenRecord.dob";
   public static final String PREDICATE_GOLDEN_RECORD_PATIENTS = "GoldenRecord.patients";
   public static final String PREDICATE_PATIENT_RECORDAUX_ID = "PatientRecord.aux_id";
   public static final String PREDICATE_PATIENT_RECORDFPID = "PatientRecord.fpid";
   public static final String PREDICATE_PATIENT_RECORDGENDER = "PatientRecord.gender";
   public static final String PREDICATE_PATIENT_RECORDDOB = "PatientRecord.dob";

   static final String GOLDEN_RECORD_FIELD_NAMES =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_id
         GoldenRecord.fpid
         GoldenRecord.gender
         GoldenRecord.dob
         """;

   static final String EXPANDED_GOLDEN_RECORD_FIELD_NAMES =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_id
         GoldenRecord.fpid
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.patients @facets(score) {
            uid
            PatientRecord.source_id {
               uid
               SourceId.facility
               SourceId.patient
            }
            PatientRecord.aux_id
            PatientRecord.fpid
            PatientRecord.gender
            PatientRecord.dob
         }
         """;
   static final String PATIENT_RECORD_FIELD_NAMES =
         """
         uid
         PatientRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         PatientRecord.aux_id
         PatientRecord.fpid
         PatientRecord.gender
         PatientRecord.dob
         """;
   static final String EXPANDED_PATIENT_RECORD_FIELD_NAMES =
         """
         uid
         PatientRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         PatientRecord.aux_id
         PatientRecord.fpid
         PatientRecord.gender
         PatientRecord.dob
         ~GoldenRecord.patients @facets(score) {
            uid
            GoldenRecord.source_id {
              uid
              SourceId.facility
              SourceId.patient
            }
            GoldenRecord.aux_id
            GoldenRecord.fpid
            GoldenRecord.gender
            GoldenRecord.dob
         }
         """;

   static final String QUERY_GET_PATIENT_BY_UID =
         """
         query patientByUid($uid: string) {
            all(func: uid($uid)) {
               uid
               PatientRecord.source_id {
                 uid
                 SourceId.facility
                 SourceId.patient
               }
               PatientRecord.aux_id
               PatientRecord.fpid
               PatientRecord.gender
               PatientRecord.dob
            }
         }
         """;

   static final String QUERY_GET_GOLDEN_RECORD_BY_UID =
         """
         query goldenRecordByUid($uid: string) {
            all(func: uid($uid)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_id
               GoldenRecord.fpid
               GoldenRecord.gender
               GoldenRecord.dob
            }
         }
         """;

   static final String QUERY_GET_EXPANDED_PATIENTS =
         """
         query expandedPatient() {
            all(func: uid(%s)) {
               uid
               PatientRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               PatientRecord.aux_id
               PatientRecord.fpid
               PatientRecord.gender
               PatientRecord.dob
               ~GoldenRecord.patients @facets(score) {
                  uid
                  GoldenRecord.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  GoldenRecord.aux_id
                  GoldenRecord.fpid
                  GoldenRecord.gender
                  GoldenRecord.dob
               }
            }
         }
         """;

   static final String QUERY_GET_GOLDEN_RECORDS =
         """
         query goldenRecord() {
            all(func: uid(%s)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_id
               GoldenRecord.fpid
               GoldenRecord.gender
               GoldenRecord.dob
            }
         }
         """;

   static final String QUERY_GET_EXPANDED_GOLDEN_RECORDS =
         """
         query expandedGoldenRecord() {
            all(func: uid(%s)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_id
               GoldenRecord.fpid
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.patients @facets(score) {
                  uid
                  PatientRecord.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  PatientRecord.aux_id
                  PatientRecord.fpid
                  PatientRecord.gender
                  PatientRecord.dob
               }
            }
         }
         """;

   static final String MUTATION_CREATE_SOURCE_ID_TYPE =
         """
         type SourceId {
            SourceId.facility
            SourceId.patient
         }
         """;
     
   static final String MUTATION_CREATE_SOURCE_ID_FIELDS =
         """
         SourceId.facility:                     string    @index(exact)                      .
         SourceId.patient:                      string    @index(exact)                      .
         """;
       
   static final String MUTATION_CREATE_GOLDEN_RECORD_TYPE =
         """

         type GoldenRecord {
            GoldenRecord.source_id:                 [SourceId]
            GoldenRecord.aux_id
            GoldenRecord.fpid
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.patients:                  [PatientRecord]
         }
         """;
         
   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
         """
         GoldenRecord.source_id:                [uid]                                        .
         GoldenRecord.aux_id:                   string    @index(exact)                      .
         GoldenRecord.fpid:                     string    @index(exact)                      .
         GoldenRecord.gender:                   string    @index(exact)                      .
         GoldenRecord.dob:                      string    @index(exact)                      .
         GoldenRecord.patients:                 [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_PATIENT_TYPE =
         """

         type PatientRecord {
            PatientRecord.source_id:                     SourceId
            PatientRecord.aux_id
            PatientRecord.fpid
            PatientRecord.gender
            PatientRecord.dob
         }
         """;

   static final String MUTATION_CREATE_PATIENT_FIELDS =
         """
         PatientRecord.source_id:                    uid                                          .
         PatientRecord.aux_id:                       string                                       .
         PatientRecord.fpid:                         string    @index(exact)                      .
         PatientRecord.gender:                       string    @index(exact)                      .
         PatientRecord.dob:                          string    @index(exact)                      .
         """;

   private CustomDgraphConstants() {}

}
