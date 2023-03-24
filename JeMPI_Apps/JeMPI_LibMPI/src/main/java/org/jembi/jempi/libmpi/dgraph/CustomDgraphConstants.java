package org.jembi.jempi.libmpi.dgraph;

public final class CustomDgraphConstants {

   public static final String PREDICATE_GOLDEN_RECORD_AUX_ID = "GoldenRecord.aux_id";
   public static final String PREDICATE_GOLDEN_RECORD_GIVEN_NAME = "GoldenRecord.given_name";
   public static final String PREDICATE_GOLDEN_RECORD_FAMILY_NAME = "GoldenRecord.family_name";
   public static final String PREDICATE_GOLDEN_RECORD_GENDER = "GoldenRecord.gender";
   public static final String PREDICATE_GOLDEN_RECORD_DOB = "GoldenRecord.dob";
   public static final String PREDICATE_GOLDEN_RECORD_CITY = "GoldenRecord.city";
   public static final String PREDICATE_GOLDEN_RECORD_PHONE_NUMBER = "GoldenRecord.phone_number";
   public static final String PREDICATE_GOLDEN_RECORD_NATIONAL_ID = "GoldenRecord.national_id";
   public static final String PREDICATE_GOLDEN_RECORD_PATIENTS = "GoldenRecord.patients";
   public static final String PREDICATE_PATIENT_RECORDAUX_ID = "PatientRecord.aux_id";
   public static final String PREDICATE_PATIENT_RECORDGIVEN_NAME = "PatientRecord.given_name";
   public static final String PREDICATE_PATIENT_RECORDFAMILY_NAME = "PatientRecord.family_name";
   public static final String PREDICATE_PATIENT_RECORDGENDER = "PatientRecord.gender";
   public static final String PREDICATE_PATIENT_RECORDDOB = "PatientRecord.dob";
   public static final String PREDICATE_PATIENT_RECORDCITY = "PatientRecord.city";
   public static final String PREDICATE_PATIENT_RECORDPHONE_NUMBER = "PatientRecord.phone_number";
   public static final String PREDICATE_PATIENT_RECORDNATIONAL_ID = "PatientRecord.national_id";

   static final String GOLDEN_RECORD_FIELD_NAMES =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_id
         GoldenRecord.given_name
         GoldenRecord.family_name
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.city
         GoldenRecord.phone_number
         GoldenRecord.national_id
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
         GoldenRecord.given_name
         GoldenRecord.family_name
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.city
         GoldenRecord.phone_number
         GoldenRecord.national_id
         GoldenRecord.patients @facets(score) {
            uid
            PatientRecord.source_id {
               uid
               SourceId.facility
               SourceId.patient
            }
            PatientRecord.aux_id
            PatientRecord.given_name
            PatientRecord.family_name
            PatientRecord.gender
            PatientRecord.dob
            PatientRecord.city
            PatientRecord.phone_number
            PatientRecord.national_id
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
         PatientRecord.given_name
         PatientRecord.family_name
         PatientRecord.gender
         PatientRecord.dob
         PatientRecord.city
         PatientRecord.phone_number
         PatientRecord.national_id
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
         PatientRecord.given_name
         PatientRecord.family_name
         PatientRecord.gender
         PatientRecord.dob
         PatientRecord.city
         PatientRecord.phone_number
         PatientRecord.national_id
         ~GoldenRecord.patients @facets(score) {
            uid
            GoldenRecord.source_id {
              uid
              SourceId.facility
              SourceId.patient
            }
            GoldenRecord.aux_id
            GoldenRecord.given_name
            GoldenRecord.family_name
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.city
            GoldenRecord.phone_number
            GoldenRecord.national_id
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
               PatientRecord.given_name
               PatientRecord.family_name
               PatientRecord.gender
               PatientRecord.dob
               PatientRecord.city
               PatientRecord.phone_number
               PatientRecord.national_id
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
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
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
               PatientRecord.given_name
               PatientRecord.family_name
               PatientRecord.gender
               PatientRecord.dob
               PatientRecord.city
               PatientRecord.phone_number
               PatientRecord.national_id
               ~GoldenRecord.patients @facets(score) {
                  uid
                  GoldenRecord.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  GoldenRecord.aux_id
                  GoldenRecord.given_name
                  GoldenRecord.family_name
                  GoldenRecord.gender
                  GoldenRecord.dob
                  GoldenRecord.city
                  GoldenRecord.phone_number
                  GoldenRecord.national_id
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
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
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
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
               GoldenRecord.patients @facets(score) {
                  uid
                  PatientRecord.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  PatientRecord.aux_id
                  PatientRecord.given_name
                  PatientRecord.family_name
                  PatientRecord.gender
                  PatientRecord.dob
                  PatientRecord.city
                  PatientRecord.phone_number
                  PatientRecord.national_id
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
            GoldenRecord.given_name
            GoldenRecord.family_name
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.city
            GoldenRecord.phone_number
            GoldenRecord.national_id
            GoldenRecord.patients:                  [PatientRecord]
         }
         """;
         
   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
         """
         GoldenRecord.source_id:                [uid]                                        .
         GoldenRecord.aux_id:                   string    @index(exact)                      .
         GoldenRecord.given_name:               string    @index(exact,trigram)              .
         GoldenRecord.family_name:              string    @index(exact,trigram)              .
         GoldenRecord.gender:                   string    @index(exact,trigram)              .
         GoldenRecord.dob:                      string                                       .
         GoldenRecord.city:                     string    @index(trigram)                    .
         GoldenRecord.phone_number:             string    @index(exact,trigram)              .
         GoldenRecord.national_id:              string    @index(exact,trigram)              .
         GoldenRecord.patients:                 [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_PATIENT_TYPE =
         """

         type PatientRecord {
            PatientRecord.source_id:                     SourceId
            PatientRecord.aux_id
            PatientRecord.given_name
            PatientRecord.family_name
            PatientRecord.gender
            PatientRecord.dob
            PatientRecord.city
            PatientRecord.phone_number
            PatientRecord.national_id
         }
         """;

   static final String MUTATION_CREATE_PATIENT_FIELDS =
         """
         PatientRecord.source_id:                    uid                                          .
         PatientRecord.aux_id:                       string                                       .
         PatientRecord.given_name:                   string    @index(exact,trigram)              .
         PatientRecord.family_name:                  string    @index(exact,trigram)              .
         PatientRecord.gender:                       string                                       .
         PatientRecord.dob:                          string                                       .
         PatientRecord.city:                         string                                       .
         PatientRecord.phone_number:                 string                                       .
         PatientRecord.national_id:                  string    @index(exact,trigram)              .
         """;

   private CustomDgraphConstants() {}

}
