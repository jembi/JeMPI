package org.jembi.jempi.shared.config;

final class TestConstants {

   static final String CONFIG_FILE_11 = "config-reference.json";
   static final String CONFIG_FILE_12 = "config-reference-link-d-validate-dp-match-dp.json";

   static final String GOLDEN_RECORD_FIELD_NAMES_1 =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_date_created
         GoldenRecord.aux_auto_update_enabled
         GoldenRecord.aux_id
         GoldenRecord.given_name
         GoldenRecord.family_name
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.city
         GoldenRecord.phone_number
         GoldenRecord.national_id
         """;

   static final String EXPANDED_GOLDEN_RECORD_FIELD_NAMES_1 =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_date_created
         GoldenRecord.aux_auto_update_enabled
         GoldenRecord.aux_id
         GoldenRecord.given_name
         GoldenRecord.family_name
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.city
         GoldenRecord.phone_number
         GoldenRecord.national_id
         GoldenRecord.interactions @facets(score) {
            uid
            Interaction.source_id {
               uid
               SourceId.facility
               SourceId.patient
            }
            Interaction.aux_date_created
            Interaction.aux_id
            Interaction.aux_clinical_data
            Interaction.given_name
            Interaction.family_name
            Interaction.gender
            Interaction.dob
            Interaction.city
            Interaction.phone_number
            Interaction.national_id
         }
         """;

   static final String INTERACTION_FIELD_NAMES_1 =
         """
         uid
         Interaction.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         Interaction.aux_date_created
         Interaction.aux_id
         Interaction.aux_clinical_data
         Interaction.given_name
         Interaction.family_name
         Interaction.gender
         Interaction.dob
         Interaction.city
         Interaction.phone_number
         Interaction.national_id
         """;

   static final String EXPANDED_INTERACTION_FIELD_NAMES_1 =
         """
         uid
         Interaction.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         Interaction.aux_date_created
         Interaction.aux_id
         Interaction.aux_clinical_data
         Interaction.given_name
         Interaction.family_name
         Interaction.gender
         Interaction.dob
         Interaction.city
         Interaction.phone_number
         Interaction.national_id
         ~GoldenRecord.interactions @facets(score) {
            uid
            GoldenRecord.source_id {
              uid
              SourceId.facility
              SourceId.patient
            }
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_INTERACTION_BY_UID_1 =
         """
         query interactionByUid($uid: string) {
            all(func: uid($uid)) {
               uid
               Interaction.source_id {
                 uid
                 SourceId.facility
                 SourceId.patient
               }
               Interaction.aux_date_created
               Interaction.aux_id
               Interaction.aux_clinical_data
               Interaction.given_name
               Interaction.family_name
               Interaction.gender
               Interaction.dob
               Interaction.city
               Interaction.phone_number
               Interaction.national_id
            }
         }
         """;

   static final String QUERY_GET_GOLDEN_RECORD_BY_UID_1 =
         """
         query goldenRecordByUid($uid: string) {
            all(func: uid($uid)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_EXPANDED_INTERACTIONS_1 =
         """
         query expandedInteraction() {
            all(func: uid(%s)) {
               uid
               Interaction.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               Interaction.aux_date_created
               Interaction.aux_id
               Interaction.aux_clinical_data
               Interaction.given_name
               Interaction.family_name
               Interaction.gender
               Interaction.dob
               Interaction.city
               Interaction.phone_number
               Interaction.national_id
               ~GoldenRecord.interactions @facets(score) {
                  uid
                  GoldenRecord.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  GoldenRecord.aux_date_created
                  GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_GOLDEN_RECORDS_1 =
         """
         query goldenRecord() {
            all(func: uid(%s)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_EXPANDED_GOLDEN_RECORDS_1 =
         """
         query expandedGoldenRecord() {
            all(func: uid(%s), orderdesc: GoldenRecord.aux_date_created) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
               GoldenRecord.interactions @facets(score) {
                  uid
                  Interaction.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  Interaction.aux_date_created
                  Interaction.aux_id
                  Interaction.aux_clinical_data
                  Interaction.given_name
                  Interaction.family_name
                  Interaction.gender
                  Interaction.dob
                  Interaction.city
                  Interaction.phone_number
                  Interaction.national_id
               }
            }
         }
         """;

   static final String MUTATION_CREATE_SOURCE_ID_TYPE_1 =
         """
         type SourceId {
            SourceId.facility
            SourceId.patient
         }
         """;

   static final String MUTATION_CREATE_SOURCE_ID_FIELDS_1 =
         """
         SourceId.facility:                     string    @index(exact)                      .
         SourceId.patient:                      string    @index(exact)                      .
         """;

   static final String MUTATION_CREATE_GOLDEN_RECORD_TYPE_1 =
         """
         type GoldenRecord {
            GoldenRecord.source_id:                 [SourceId]
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
            GoldenRecord.aux_id
            GoldenRecord.given_name
            GoldenRecord.family_name
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.city
            GoldenRecord.phone_number
            GoldenRecord.national_id
            GoldenRecord.interactions:              [Interaction]
         }
         """;

   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS_1 =
         """
         GoldenRecord.source_id:                [uid]     @reverse                           .
         GoldenRecord.aux_date_created:         datetime                                     .
         GoldenRecord.aux_auto_update_enabled:  bool                                         .
         GoldenRecord.aux_id:                   string                                       .
         GoldenRecord.given_name:               string    @index(exact,trigram)              .
         GoldenRecord.family_name:              string    @index(exact,trigram)              .
         GoldenRecord.gender:                   string    @index(exact,trigram)              .
         GoldenRecord.dob:                      string                                       .
         GoldenRecord.city:                     string    @index(trigram)                    .
         GoldenRecord.phone_number:             string    @index(exact,trigram)              .
         GoldenRecord.national_id:              string    @index(exact,trigram)              .
         GoldenRecord.interactions:             [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_INTERACTION_TYPE_1 =
         """
         type Interaction {
            Interaction.source_id:                     SourceId
            Interaction.aux_date_created
            Interaction.aux_id
            Interaction.aux_clinical_data
            Interaction.given_name
            Interaction.family_name
            Interaction.gender
            Interaction.dob
            Interaction.city
            Interaction.phone_number
            Interaction.national_id
         }
         """;

   static final String MUTATION_CREATE_INTERACTION_FIELDS_1 =
         """
         Interaction.source_id:                    uid                                          .
         Interaction.aux_date_created:             datetime                                     .
         Interaction.aux_id:                       string                                       .
         Interaction.aux_clinical_data:            string                                       .
         Interaction.given_name:                   string    @index(exact,trigram)              .
         Interaction.family_name:                  string    @index(exact,trigram)              .
         Interaction.gender:                       string                                       .
         Interaction.dob:                          string                                       .
         Interaction.city:                         string                                       .
         Interaction.phone_number:                 string                                       .
         Interaction.national_id:                  string    @index(exact,trigram)              .
         """;

   static final String GOLDEN_RECORD_FIELD_NAMES_2 =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_date_created
         GoldenRecord.aux_auto_update_enabled
         GoldenRecord.aux_id
         GoldenRecord.given_name
         GoldenRecord.family_name
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.city
         GoldenRecord.phone_number
         GoldenRecord.national_id
         """;

   static final String EXPANDED_GOLDEN_RECORD_FIELD_NAMES_2 =
         """
         uid
         GoldenRecord.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         GoldenRecord.aux_date_created
         GoldenRecord.aux_auto_update_enabled
         GoldenRecord.aux_id
         GoldenRecord.given_name
         GoldenRecord.family_name
         GoldenRecord.gender
         GoldenRecord.dob
         GoldenRecord.city
         GoldenRecord.phone_number
         GoldenRecord.national_id
         GoldenRecord.interactions @facets(score) {
            uid
            Interaction.source_id {
               uid
               SourceId.facility
               SourceId.patient
            }
            Interaction.aux_date_created
            Interaction.aux_id
            Interaction.aux_clinical_data
            Interaction.given_name
            Interaction.family_name
            Interaction.gender
            Interaction.dob
            Interaction.city
            Interaction.phone_number
            Interaction.national_id
         }
         """;

   static final String INTERACTION_FIELD_NAMES_2 =
         """
         uid
         Interaction.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         Interaction.aux_date_created
         Interaction.aux_id
         Interaction.aux_clinical_data
         Interaction.given_name
         Interaction.family_name
         Interaction.gender
         Interaction.dob
         Interaction.city
         Interaction.phone_number
         Interaction.national_id
         """;

   static final String EXPANDED_INTERACTION_FIELD_NAMES_2 =
         """
         uid
         Interaction.source_id {
            uid
            SourceId.facility
            SourceId.patient
         }
         Interaction.aux_date_created
         Interaction.aux_id
         Interaction.aux_clinical_data
         Interaction.given_name
         Interaction.family_name
         Interaction.gender
         Interaction.dob
         Interaction.city
         Interaction.phone_number
         Interaction.national_id
         ~GoldenRecord.interactions @facets(score) {
            uid
            GoldenRecord.source_id {
              uid
              SourceId.facility
              SourceId.patient
            }
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_INTERACTION_BY_UID_2 =
         """
         query interactionByUid($uid: string) {
            all(func: uid($uid)) {
               uid
               Interaction.source_id {
                 uid
                 SourceId.facility
                 SourceId.patient
               }
               Interaction.aux_date_created
               Interaction.aux_id
               Interaction.aux_clinical_data
               Interaction.given_name
               Interaction.family_name
               Interaction.gender
               Interaction.dob
               Interaction.city
               Interaction.phone_number
               Interaction.national_id
            }
         }
         """;

   static final String QUERY_GET_GOLDEN_RECORD_BY_UID_2 =
         """
         query goldenRecordByUid($uid: string) {
            all(func: uid($uid)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_EXPANDED_INTERACTIONS_2 =
         """
         query expandedInteraction() {
            all(func: uid(%s)) {
               uid
               Interaction.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               Interaction.aux_date_created
               Interaction.aux_id
               Interaction.aux_clinical_data
               Interaction.given_name
               Interaction.family_name
               Interaction.gender
               Interaction.dob
               Interaction.city
               Interaction.phone_number
               Interaction.national_id
               ~GoldenRecord.interactions @facets(score) {
                  uid
                  GoldenRecord.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  GoldenRecord.aux_date_created
                  GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_GOLDEN_RECORDS_2 =
         """
         query goldenRecord() {
            all(func: uid(%s)) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
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

   static final String QUERY_GET_EXPANDED_GOLDEN_RECORDS_2 =
         """
         query expandedGoldenRecord() {
            all(func: uid(%s), orderdesc: GoldenRecord.aux_date_created) {
               uid
               GoldenRecord.source_id {
                  uid
                  SourceId.facility
                  SourceId.patient
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
               GoldenRecord.interactions @facets(score) {
                  uid
                  Interaction.source_id {
                    uid
                    SourceId.facility
                    SourceId.patient
                  }
                  Interaction.aux_date_created
                  Interaction.aux_id
                  Interaction.aux_clinical_data
                  Interaction.given_name
                  Interaction.family_name
                  Interaction.gender
                  Interaction.dob
                  Interaction.city
                  Interaction.phone_number
                  Interaction.national_id
               }
            }
         }
         """;

   static final String MUTATION_CREATE_SOURCE_ID_TYPE_2 =
         """
         type SourceId {
            SourceId.facility
            SourceId.patient
         }
         """;

   static final String MUTATION_CREATE_SOURCE_ID_FIELDS_2 =
         """
         SourceId.facility:                     string    @index(exact)                      .
         SourceId.patient:                      string    @index(exact)                      .
         """;

   static final String MUTATION_CREATE_GOLDEN_RECORD_TYPE_2 =
         """
         type GoldenRecord {
            GoldenRecord.source_id:                 [SourceId]
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
            GoldenRecord.aux_id
            GoldenRecord.given_name
            GoldenRecord.family_name
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.city
            GoldenRecord.phone_number
            GoldenRecord.national_id
            GoldenRecord.interactions:              [Interaction]
         }
         """;

   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS_2 =
         """
         GoldenRecord.source_id:                [uid]     @reverse                           .
         GoldenRecord.aux_date_created:         datetime                                     .
         GoldenRecord.aux_auto_update_enabled:  bool                                         .
         GoldenRecord.aux_id:                   string                                       .
         GoldenRecord.given_name:               string    @index(exact,trigram)              .
         GoldenRecord.family_name:              string    @index(exact,trigram)              .
         GoldenRecord.gender:                   string    @index(exact,trigram)              .
         GoldenRecord.dob:                      string                                       .
         GoldenRecord.city:                     string    @index(exact,trigram)              .
         GoldenRecord.phone_number:             string    @index(exact,trigram)              .
         GoldenRecord.national_id:              string    @index(exact)                      .
         GoldenRecord.interactions:             [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_INTERACTION_TYPE_2 =
         """
         type Interaction {
            Interaction.source_id:                     SourceId
            Interaction.aux_date_created
            Interaction.aux_id
            Interaction.aux_clinical_data
            Interaction.given_name
            Interaction.family_name
            Interaction.gender
            Interaction.dob
            Interaction.city
            Interaction.phone_number
            Interaction.national_id
         }
         """;

   static final String MUTATION_CREATE_INTERACTION_FIELDS_2 =
         """
         Interaction.source_id:                    uid                                          .
         Interaction.aux_date_created:             datetime                                     .
         Interaction.aux_id:                       string                                       .
         Interaction.aux_clinical_data:            string                                       .
         Interaction.given_name:                   string                                       .
         Interaction.family_name:                  string                                       .
         Interaction.gender:                       string                                       .
         Interaction.dob:                          string                                       .
         Interaction.city:                         string                                       .
         Interaction.phone_number:                 string                                       .
         Interaction.national_id:                  string                                       .
         """;

   private TestConstants() {
   }

}
