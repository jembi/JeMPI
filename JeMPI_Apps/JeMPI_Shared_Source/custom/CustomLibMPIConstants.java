package org.jembi.jempi.libmpi.dgraph;

public final class CustomLibMPIConstants {

   private CustomLibMPIConstants() {}

   public static final String PREDICATE_GOLDEN_RECORD_AUX_ID = "GoldenRecord.aux_id";
   public static final String PREDICATE_GOLDEN_RECORD_GIVEN_NAME = "GoldenRecord.given_name";
   public static final String PREDICATE_GOLDEN_RECORD_FAMILY_NAME = "GoldenRecord.family_name";
   public static final String PREDICATE_GOLDEN_RECORD_GENDER = "GoldenRecord.gender";
   public static final String PREDICATE_GOLDEN_RECORD_DOB = "GoldenRecord.dob";
   public static final String PREDICATE_GOLDEN_RECORD_CITY = "GoldenRecord.city";
   public static final String PREDICATE_GOLDEN_RECORD_PHONE_NUMBER = "GoldenRecord.phone_number";
   public static final String PREDICATE_GOLDEN_RECORD_NATIONAL_ID = "GoldenRecord.national_id";
   public static final String PREDICATE_GOLDEN_RECORD_PATIENTS = "GoldenRecord.patients";

   public static final String PREDICATE_PATIENT_AUX_ID = "Patient.aux_id";

   public static final String PREDICATE_PATIENT_GIVEN_NAME = "Patient.given_name";

   public static final String PREDICATE_PATIENT_FAMILY_NAME = "Patient.family_name";

   public static final String PREDICATE_PATIENT_GENDER = "Patient.gender";

   public static final String PREDICATE_PATIENT_DOB = "Patient.dob";

   public static final String PREDICATE_PATIENT_CITY = "Patient.city";

   public static final String PREDICATE_PATIENT_PHONE_NUMBER = "Patient.phone_number";

   public static final String PREDICATE_PATIENT_NATIONAL_ID = "Patient.national_id";

   static final String QUERY_GET_PATIENT_BY_UID =
      """
      query patientByUid($uid: string) {
         all(func: uid($uid)) {
            uid
            Patient.source_id {
              uid
              SourceId.facility
              SourceId.patient
            }
            Patient.aux_id
            Patient.given_name
            Patient.family_name
            Patient.gender
            Patient.dob
            Patient.city
            Patient.phone_number
            Patient.national_id
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
            Patient.source_id {
               uid
               SourceId.facility
               SourceId.patient
            }
            Patient.aux_id
            Patient.given_name
            Patient.family_name
            Patient.gender
            Patient.dob
            Patient.city
            Patient.phone_number
            Patient.national_id
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
               Patient.source_id {
                 uid
                 SourceId.facility
                 SourceId.patient
               }
               Patient.aux_id
               Patient.given_name
               Patient.family_name
               Patient.gender
               Patient.dob
               Patient.city
               Patient.phone_number
               Patient.national_id
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
         GoldenRecord.patients:                  [Patient]
      }
      """;
         
   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
      """
      GoldenRecord.source_id:                [uid]                                        .
      GoldenRecord.aux_id:                   string    @index(exact)                      .
      GoldenRecord.given_name:               string    @index(exact,trigram)              .
      GoldenRecord.family_name:              string    @index(exact,trigram)              .
      GoldenRecord.gender:                   string    @index(exact)                      .
      GoldenRecord.dob:                      string                                       .
      GoldenRecord.city:                     string    @index(trigram)                    .
      GoldenRecord.phone_number:             string    @index(exact,trigram)              .
      GoldenRecord.national_id:              string    @index(exact,trigram)              .
      GoldenRecord.patients:                 [uid]     @reverse                           .
      """;

   static final String MUTATION_CREATE_PATIENT_TYPE =
      """

      type Patient {
         Patient.source_id:                     SourceId
         Patient.aux_id
         Patient.given_name
         Patient.family_name
         Patient.gender
         Patient.dob
         Patient.city
         Patient.phone_number
         Patient.national_id
      }
      """;

   static final String MUTATION_CREATE_PATIENT_FIELDS =
      """
      Patient.source_id:                    uid                                          .
      Patient.aux_id:                       string                                       .
      Patient.given_name:                   string                                       .
      Patient.family_name:                  string    @index(exact,trigram)              .
      Patient.gender:                       string                                       .
      Patient.dob:                          string                                       .
      Patient.city:                         string                                       .
      Patient.phone_number:                 string                                       .
      Patient.national_id:                  string    @index(exact,trigram)              .
      """;

}
