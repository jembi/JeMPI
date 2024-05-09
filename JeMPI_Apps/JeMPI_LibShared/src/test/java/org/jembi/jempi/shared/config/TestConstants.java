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

   static final String MUTATION_CREATE_INTERACTION_TRIPLE_1 =
         """
         _:%s  <Interaction.source_id>                     <%s>                  .
         _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime>     .
         _:%s  <Interaction.aux_id>                        %s                    .
         _:%s  <Interaction.aux_clinical_data>             %s                    .
         _:%s  <Interaction.demographic_field_00>          %s                    .
         _:%s  <Interaction.demographic_field_01>          %s                    .
         _:%s  <Interaction.demographic_field_02>          %s                    .
         _:%s  <Interaction.demographic_field_03>          %s                    .
         _:%s  <Interaction.demographic_field_04>          %s                    .
         _:%s  <Interaction.demographic_field_05>          %s                    .
         _:%s  <Interaction.demographic_field_06>          %s                    .
         _:%s  <dgraph.type>                               "Interaction"         .
         """;

   static final String MUTATION_CREATE_LINKED_GOLDEN_RECORD_TRIPLE_1 =
         """
         _:%s  <GoldenRecord.source_id>                     <%s>                  .
         _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime>     .
         _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean>      .
         _:%s  <GoldenRecord.aux_id>                        %s                    .
         _:%s  <GoldenRecord.demographic_field_00>          %s                    .
         _:%s  <GoldenRecord.demographic_field_01>          %s                    .
         _:%s  <GoldenRecord.demographic_field_02>          %s                    .
         _:%s  <GoldenRecord.demographic_field_03>          %s                    .
         _:%s  <GoldenRecord.demographic_field_04>          %s                    .
         _:%s  <GoldenRecord.demographic_field_05>          %s                    .
         _:%s  <GoldenRecord.demographic_field_06>          %s                    .
         _:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
         _:%s  <dgraph.type>                                "GoldenRecord"        .
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

   static final String MUTATION_CREATE_INTERACTION_TRIPLE_2 =
         """
         _:%s  <Interaction.source_id>                     <%s>                  .
         _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime>     .
         _:%s  <Interaction.aux_id>                        %s                    .
         _:%s  <Interaction.aux_clinical_data>             %s                    .
         _:%s  <Interaction.demographic_field_00>          %s                    .
         _:%s  <Interaction.demographic_field_01>          %s                    .
         _:%s  <Interaction.demographic_field_02>          %s                    .
         _:%s  <Interaction.demographic_field_03>          %s                    .
         _:%s  <Interaction.demographic_field_04>          %s                    .
         _:%s  <Interaction.demographic_field_05>          %s                    .
         _:%s  <Interaction.demographic_field_06>          %s                    .
         _:%s  <dgraph.type>                               "Interaction"         .
         """;

   static final String MUTATION_CREATE_LINKED_GOLDEN_RECORD_TRIPLE_2 =
         """
         _:%s  <GoldenRecord.source_id>                     <%s>                  .
         _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime>     .
         _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean>      .
         _:%s  <GoldenRecord.aux_id>                        %s                    .
         _:%s  <GoldenRecord.demographic_field_00>          %s                    .
         _:%s  <GoldenRecord.demographic_field_01>          %s                    .
         _:%s  <GoldenRecord.demographic_field_02>          %s                    .
         _:%s  <GoldenRecord.demographic_field_03>          %s                    .
         _:%s  <GoldenRecord.demographic_field_04>          %s                    .
         _:%s  <GoldenRecord.demographic_field_05>          %s                    .
         _:%s  <GoldenRecord.demographic_field_06>          %s                    .
         _:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
         _:%s  <dgraph.type>                                "GoldenRecord"        .
         """;

   static final String SELECT_QUERY_LINK_DETERMINISTIC_A_1 =
         """
         query query_link_deterministic_00($national_id: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_06,$national_id)) {
               uid
               GoldenRecord.source_id {
                  uid
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

   static final String SELECT_QUERY_LINK_DETERMINISTIC_B_1 =
         """
         query query_link_deterministic_01($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_00, $given_name)) {
               A as uid
            }
         
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_01, $family_name)) {
               B as uid
            }
         
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_05, $phone_number)) {
               C as uid
            }
         
            all(func:type(GoldenRecord)) @filter(((uid(A) AND uid(B)) AND uid(C))) {
               uid
               GoldenRecord.source_id {
                  uid
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

   static final String SELECT_QUERY_LINK_PROBABILISTIC_BLOCK_1 =
         """
         query query_link_probabilistic_block($given_name: string, $family_name: string, $city: string, $phone_number: string, $national_id: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_04, $city,3)) {
               C as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_05, $phone_number,2)) {
               D as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_01, $family_name,3)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_06, $national_id,3)) {
               E as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_00, $given_name,3)) {
               A as uid
            }
            all(func:type(GoldenRecord)) @filter(((uid(A) AND uid(B)) OR (uid(A) AND uid(C)) OR (uid(B) AND uid(C))) OR uid(D) OR uid(E)) {
               uid
               GoldenRecord.source_id {
                  uid
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

   static final String SELECT_QUERY_LINK_BLOCK_00_1 =
         """
         query query_link_deterministic_00($given_name: string, $family_name: string, $city: string, $phone_number: string, $national_id: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_00, $given_name, 3)) {
               A as uid
            }
         
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_01, $family_name, 3)) {
               B as uid
            }
         
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_04, $city, 3)) {
               C as uid
            }
         
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_05, $phone_number, 2)) {
               D as uid
            }
         
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_06, $national_id, 3)) {
               E as uid
            }
         
            all(func:type(GoldenRecord)) @filter((((((uid(A) AND uid(B)) OR (uid(A) AND uid(C))) OR (uid(B) AND uid(C))) OR uid(D)) OR uid(E))) {
               uid
               GoldenRecord.source_id {
                  uid
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

   private TestConstants() {
   }

}
