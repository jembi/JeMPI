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
         GoldenRecord.demographic_field_00
         GoldenRecord.demographic_field_01
         GoldenRecord.demographic_field_02
         GoldenRecord.demographic_field_03
         GoldenRecord.demographic_field_04
         GoldenRecord.demographic_field_05
         GoldenRecord.demographic_field_06
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
         GoldenRecord.demographic_field_00
         GoldenRecord.demographic_field_01
         GoldenRecord.demographic_field_02
         GoldenRecord.demographic_field_03
         GoldenRecord.demographic_field_04
         GoldenRecord.demographic_field_05
         GoldenRecord.demographic_field_06
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
            Interaction.demographic_field_00
            Interaction.demographic_field_01
            Interaction.demographic_field_02
            Interaction.demographic_field_03
            Interaction.demographic_field_04
            Interaction.demographic_field_05
            Interaction.demographic_field_06
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
         Interaction.demographic_field_00
         Interaction.demographic_field_01
         Interaction.demographic_field_02
         Interaction.demographic_field_03
         Interaction.demographic_field_04
         Interaction.demographic_field_05
         Interaction.demographic_field_06
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
         Interaction.demographic_field_00
         Interaction.demographic_field_01
         Interaction.demographic_field_02
         Interaction.demographic_field_03
         Interaction.demographic_field_04
         Interaction.demographic_field_05
         Interaction.demographic_field_06
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
            GoldenRecord.demographic_field_00
            GoldenRecord.demographic_field_01
            GoldenRecord.demographic_field_02
            GoldenRecord.demographic_field_03
            GoldenRecord.demographic_field_04
            GoldenRecord.demographic_field_05
            GoldenRecord.demographic_field_06
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
               Interaction.demographic_field_00
               Interaction.demographic_field_01
               Interaction.demographic_field_02
               Interaction.demographic_field_03
               Interaction.demographic_field_04
               Interaction.demographic_field_05
               Interaction.demographic_field_06
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
               GoldenRecord.demographic_field_00
               GoldenRecord.demographic_field_01
               GoldenRecord.demographic_field_02
               GoldenRecord.demographic_field_03
               GoldenRecord.demographic_field_04
               GoldenRecord.demographic_field_05
               GoldenRecord.demographic_field_06
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
               Interaction.demographic_field_00
               Interaction.demographic_field_01
               Interaction.demographic_field_02
               Interaction.demographic_field_03
               Interaction.demographic_field_04
               Interaction.demographic_field_05
               Interaction.demographic_field_06
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
                  GoldenRecord.demographic_field_00
                  GoldenRecord.demographic_field_01
                  GoldenRecord.demographic_field_02
                  GoldenRecord.demographic_field_03
                  GoldenRecord.demographic_field_04
                  GoldenRecord.demographic_field_05
                  GoldenRecord.demographic_field_06
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
               GoldenRecord.demographic_field_00
               GoldenRecord.demographic_field_01
               GoldenRecord.demographic_field_02
               GoldenRecord.demographic_field_03
               GoldenRecord.demographic_field_04
               GoldenRecord.demographic_field_05
               GoldenRecord.demographic_field_06
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
               GoldenRecord.demographic_field_00
               GoldenRecord.demographic_field_01
               GoldenRecord.demographic_field_02
               GoldenRecord.demographic_field_03
               GoldenRecord.demographic_field_04
               GoldenRecord.demographic_field_05
               GoldenRecord.demographic_field_06
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
                  Interaction.demographic_field_00
                  Interaction.demographic_field_01
                  Interaction.demographic_field_02
                  Interaction.demographic_field_03
                  Interaction.demographic_field_04
                  Interaction.demographic_field_05
                  Interaction.demographic_field_06
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
            GoldenRecord.source_id:                             [SourceId]
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
            GoldenRecord.aux_id
            GoldenRecord.demographic_field_00
            GoldenRecord.demographic_field_01
            GoldenRecord.demographic_field_02
            GoldenRecord.demographic_field_03
            GoldenRecord.demographic_field_04
            GoldenRecord.demographic_field_05
            GoldenRecord.demographic_field_06
            GoldenRecord.interactions:                          [Interaction]
         }
         """;

   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS_1 =
         """
         GoldenRecord.source_id:                   [uid]     @reverse                           .
         GoldenRecord.aux_date_created:            datetime                                     .
         GoldenRecord.aux_auto_update_enabled:     bool                                         .
         GoldenRecord.aux_id:                      string                                       .
         GoldenRecord.demographic_field_00:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_01:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_02:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_03:        string                                       .
         GoldenRecord.demographic_field_04:        string    @index(trigram)                    .
         GoldenRecord.demographic_field_05:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_06:        string    @index(exact,trigram)              .
         GoldenRecord.interactions:                [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_INTERACTION_TYPE_1 =
         """
         type Interaction {
            Interaction.source_id:                     SourceId
            Interaction.aux_date_created
            Interaction.aux_id
            Interaction.aux_clinical_data
            Interaction.demographic_field_00
            Interaction.demographic_field_01
            Interaction.demographic_field_02
            Interaction.demographic_field_03
            Interaction.demographic_field_04
            Interaction.demographic_field_05
            Interaction.demographic_field_06
         }
         """;

   static final String MUTATION_CREATE_INTERACTION_FIELDS_1 =
         """
         Interaction.source_id:                    uid                                          .
         Interaction.aux_date_created:             datetime                                     .
         Interaction.aux_id:                       string                                       .
         Interaction.aux_clinical_data:            string                                       .
         Interaction.demographic_field_00:         string    @index(exact,trigram)              .
         Interaction.demographic_field_01:         string    @index(exact,trigram)              .
         Interaction.demographic_field_02:         string                                       .
         Interaction.demographic_field_03:         string                                       .
         Interaction.demographic_field_04:         string                                       .
         Interaction.demographic_field_05:         string                                       .
         Interaction.demographic_field_06:         string    @index(exact,trigram)              .
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
         GoldenRecord.demographic_field_00
         GoldenRecord.demographic_field_01
         GoldenRecord.demographic_field_02
         GoldenRecord.demographic_field_03
         GoldenRecord.demographic_field_04
         GoldenRecord.demographic_field_05
         GoldenRecord.demographic_field_06
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
         GoldenRecord.demographic_field_00
         GoldenRecord.demographic_field_01
         GoldenRecord.demographic_field_02
         GoldenRecord.demographic_field_03
         GoldenRecord.demographic_field_04
         GoldenRecord.demographic_field_05
         GoldenRecord.demographic_field_06
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
            Interaction.demographic_field_00
            Interaction.demographic_field_01
            Interaction.demographic_field_02
            Interaction.demographic_field_03
            Interaction.demographic_field_04
            Interaction.demographic_field_05
            Interaction.demographic_field_06
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
         Interaction.demographic_field_00
         Interaction.demographic_field_01
         Interaction.demographic_field_02
         Interaction.demographic_field_03
         Interaction.demographic_field_04
         Interaction.demographic_field_05
         Interaction.demographic_field_06
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
         Interaction.demographic_field_00
         Interaction.demographic_field_01
         Interaction.demographic_field_02
         Interaction.demographic_field_03
         Interaction.demographic_field_04
         Interaction.demographic_field_05
         Interaction.demographic_field_06
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
            GoldenRecord.demographic_field_00
            GoldenRecord.demographic_field_01
            GoldenRecord.demographic_field_02
            GoldenRecord.demographic_field_03
            GoldenRecord.demographic_field_04
            GoldenRecord.demographic_field_05
            GoldenRecord.demographic_field_06
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
               Interaction.demographic_field_00
               Interaction.demographic_field_01
               Interaction.demographic_field_02
               Interaction.demographic_field_03
               Interaction.demographic_field_04
               Interaction.demographic_field_05
               Interaction.demographic_field_06
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
               GoldenRecord.demographic_field_00
               GoldenRecord.demographic_field_01
               GoldenRecord.demographic_field_02
               GoldenRecord.demographic_field_03
               GoldenRecord.demographic_field_04
               GoldenRecord.demographic_field_05
               GoldenRecord.demographic_field_06
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
               Interaction.demographic_field_00
               Interaction.demographic_field_01
               Interaction.demographic_field_02
               Interaction.demographic_field_03
               Interaction.demographic_field_04
               Interaction.demographic_field_05
               Interaction.demographic_field_06
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
                  GoldenRecord.demographic_field_00
                  GoldenRecord.demographic_field_01
                  GoldenRecord.demographic_field_02
                  GoldenRecord.demographic_field_03
                  GoldenRecord.demographic_field_04
                  GoldenRecord.demographic_field_05
                  GoldenRecord.demographic_field_06
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
               GoldenRecord.demographic_field_00
               GoldenRecord.demographic_field_01
               GoldenRecord.demographic_field_02
               GoldenRecord.demographic_field_03
               GoldenRecord.demographic_field_04
               GoldenRecord.demographic_field_05
               GoldenRecord.demographic_field_06
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
               GoldenRecord.demographic_field_00
               GoldenRecord.demographic_field_01
               GoldenRecord.demographic_field_02
               GoldenRecord.demographic_field_03
               GoldenRecord.demographic_field_04
               GoldenRecord.demographic_field_05
               GoldenRecord.demographic_field_06
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
                  Interaction.demographic_field_00
                  Interaction.demographic_field_01
                  Interaction.demographic_field_02
                  Interaction.demographic_field_03
                  Interaction.demographic_field_04
                  Interaction.demographic_field_05
                  Interaction.demographic_field_06
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
            GoldenRecord.source_id:                             [SourceId]
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
            GoldenRecord.aux_id
            GoldenRecord.demographic_field_00
            GoldenRecord.demographic_field_01
            GoldenRecord.demographic_field_02
            GoldenRecord.demographic_field_03
            GoldenRecord.demographic_field_04
            GoldenRecord.demographic_field_05
            GoldenRecord.demographic_field_06
            GoldenRecord.interactions:                          [Interaction]
         }
         """;

   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS_2 =
         """
         GoldenRecord.source_id:                   [uid]     @reverse                           .
         GoldenRecord.aux_date_created:            datetime                                     .
         GoldenRecord.aux_auto_update_enabled:     bool                                         .
         GoldenRecord.aux_id:                      string                                       .
         GoldenRecord.demographic_field_00:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_01:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_02:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_03:        string                                       .
         GoldenRecord.demographic_field_04:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_05:        string    @index(exact,trigram)              .
         GoldenRecord.demographic_field_06:        string    @index(exact)                      .
         GoldenRecord.interactions:                [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_INTERACTION_TYPE_2 =
         """
         type Interaction {
            Interaction.source_id:                     SourceId
            Interaction.aux_date_created
            Interaction.aux_id
            Interaction.aux_clinical_data
            Interaction.demographic_field_00
            Interaction.demographic_field_01
            Interaction.demographic_field_02
            Interaction.demographic_field_03
            Interaction.demographic_field_04
            Interaction.demographic_field_05
            Interaction.demographic_field_06
         }
         """;

   static final String MUTATION_CREATE_INTERACTION_FIELDS_2 =
         """
         Interaction.source_id:                    uid                                          .
         Interaction.aux_date_created:             datetime                                     .
         Interaction.aux_id:                       string                                       .
         Interaction.aux_clinical_data:            string                                       .
         Interaction.demographic_field_00:         string                                       .
         Interaction.demographic_field_01:         string                                       .
         Interaction.demographic_field_02:         string                                       .
         Interaction.demographic_field_03:         string                                       .
         Interaction.demographic_field_04:         string                                       .
         Interaction.demographic_field_05:         string                                       .
         Interaction.demographic_field_06:         string                                       .
         """;

   private TestConstants() {
   }

}
