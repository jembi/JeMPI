package org.jembi.jempi.shared.config;

import java.nio.file.FileSystems;

final class TestConstants {

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
            GoldenRecord.source_id:                             [SourceId]
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
            GoldenRecord.interactions:                          [Interaction]
         }
         """;
   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS_1 =
         """
         GoldenRecord.source_id:                   [uid]     @reverse                           .
         GoldenRecord.aux_date_created:            datetime                                     .
         GoldenRecord.aux_auto_update_enabled:     bool                                         .
         GoldenRecord.aux_id:                      string                                       .
         GoldenRecord.given_name:                  string    @index(exact,trigram)              .
         GoldenRecord.family_name:                 string    @index(exact,trigram)              .
         GoldenRecord.gender:                      string    @index(exact,trigram)              .
         GoldenRecord.dob:                         string                                       .
         GoldenRecord.city:                        string    @index(trigram)                    .
         GoldenRecord.phone_number:                string    @index(exact,trigram)              .
         GoldenRecord.national_id:                 string    @index(exact,trigram)              .
         GoldenRecord.interactions:                [uid]     @reverse                           .
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
   static final String MUTATION_CREATE_INTERACTION_TRIPLE_1 =
         """
         _:%s  <Interaction.source_id>                     <%s> .
         _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime> .
         _:%s  <Interaction.aux_id>                        %s .
         _:%s  <Interaction.aux_clinical_data>             %s .
         _:%s  <Interaction.given_name>                    %s .
         _:%s  <Interaction.family_name>                   %s .
         _:%s  <Interaction.gender>                        %s .
         _:%s  <Interaction.dob>                           %s .
         _:%s  <Interaction.city>                          %s .
         _:%s  <Interaction.phone_number>                  %s .
         _:%s  <Interaction.national_id>                   %s .
         _:%s  <dgraph.type>                               "Interaction" .
         """;
   static final String MUTATION_CREATE_LINKED_GOLDEN_RECORD_TRIPLE_1 =
         """
         _:%s  <GoldenRecord.source_id>                     <%s> .
         _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime> .
         _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean> .
         _:%s  <GoldenRecord.aux_id>                        %s .
         _:%s  <GoldenRecord.given_name>                    %s .
         _:%s  <GoldenRecord.family_name>                   %s .
         _:%s  <GoldenRecord.gender>                        %s .
         _:%s  <GoldenRecord.dob>                           %s .
         _:%s  <GoldenRecord.city>                          %s .
         _:%s  <GoldenRecord.phone_number>                  %s .
         _:%s  <GoldenRecord.national_id>                   %s .
         _:%s  <GoldenRecord.interactions>                  <%s> (score=%f) .
         _:%s  <dgraph.type>                                "GoldenRecord" .
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
            GoldenRecord.source_id:                             [SourceId]
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
            GoldenRecord.interactions:                          [Interaction]
         }
         """;
   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS_2 =
         """
         GoldenRecord.source_id:                   [uid]     @reverse                           .
         GoldenRecord.aux_date_created:            datetime                                     .
         GoldenRecord.aux_auto_update_enabled:     bool                                         .
         GoldenRecord.aux_id:                      string                                       .
         GoldenRecord.given_name:                  string    @index(exact,trigram)              .
         GoldenRecord.family_name:                 string    @index(exact,trigram)              .
         GoldenRecord.gender:                      string    @index(exact,trigram)              .
         GoldenRecord.dob:                         string                                       .
         GoldenRecord.city:                        string    @index(exact,trigram)              .
         GoldenRecord.phone_number:                string    @index(exact,trigram)              .
         GoldenRecord.national_id:                 string    @index(exact)                      .
         GoldenRecord.interactions:                [uid]     @reverse                           .
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
   static final String MUTATION_CREATE_INTERACTION_TRIPLE_2 =
         """
         _:%s  <Interaction.source_id>                     <%s> .
         _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime> .
         _:%s  <Interaction.aux_id>                        %s .
         _:%s  <Interaction.aux_clinical_data>             %s .
         _:%s  <Interaction.given_name>                    %s .
         _:%s  <Interaction.family_name>                   %s .
         _:%s  <Interaction.gender>                        %s .
         _:%s  <Interaction.dob>                           %s .
         _:%s  <Interaction.city>                          %s .
         _:%s  <Interaction.phone_number>                  %s .
         _:%s  <Interaction.national_id>                   %s .
         _:%s  <dgraph.type>                               "Interaction" .
         """;
   static final String MUTATION_CREATE_LINKED_GOLDEN_RECORD_TRIPLE_2 =
         """
         _:%s  <GoldenRecord.source_id>                     <%s> .
         _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime> .
         _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean> .
         _:%s  <GoldenRecord.aux_id>                        %s .
         _:%s  <GoldenRecord.given_name>                    %s .
         _:%s  <GoldenRecord.family_name>                   %s .
         _:%s  <GoldenRecord.gender>                        %s .
         _:%s  <GoldenRecord.dob>                           %s .
         _:%s  <GoldenRecord.city>                          %s .
         _:%s  <GoldenRecord.phone_number>                  %s .
         _:%s  <GoldenRecord.national_id>                   %s .
         _:%s  <GoldenRecord.interactions>                  <%s> (score=%f) .
         _:%s  <dgraph.type>                                "GoldenRecord" .
         """;
   static final String SELECT_QUERY_LINK_DETERMINISTIC_A_1 =
         """
         query query_link_deterministic_00($national_id: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.national_id,$national_id)) {
               uid
               GoldenRecord.source_id {
                  uid
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
   static final String SELECT_QUERY_LINK_DETERMINISTIC_B_1 =
         """
         query query_link_deterministic_01($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.given_name, $given_name)) {
               A as uid
            }
         
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.family_name, $family_name)) {
               B as uid
            }

            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.phone_number, $phone_number)) {
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
   static final String SELECT_QUERY_LINK_PROBABILISTIC_BLOCK_1 =
         """
         query query_link_probabilistic_block($given_name: string, $family_name: string, $city: string, $phone_number: string, $national_id: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.city, $city,3)) {
               C as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.phone_number, $phone_number,2)) {
               D as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.family_name, $family_name,3)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.national_id, $national_id,3)) {
               E as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.given_name, $given_name,3)) {
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
   static final String SELECT_QUERY_LINK_BLOCK_00_1 =
         """
         query query_link_deterministic_00($given_name: string, $family_name: string, $city: string, $phone_number: string, $national_id: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.city, $city, 3)) {
               match3City as uid
            }
                  
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.given_name, $given_name, 3)) {
               match3GivenName as uid
            }
                  
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.national_id, $national_id, 3)) {
               match3NationalId as uid
            }
                  
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.family_name, $family_name, 3)) {
               match3FamilyName as uid
            }
                  
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.phone_number, $phone_number, 2)) {
               match2PhoneNumber as uid
            }
                  
            all(func:type(GoldenRecord)) @filter((((((uid(match3GivenName) AND uid(match3FamilyName)) OR (uid(match3GivenName) AND uid(match3City))) OR (uid(match3FamilyName) AND uid(match3City))) OR uid(match2PhoneNumber)) OR uid(match3NationalId))) {
               uid
               GoldenRecord.source_id {
                  uid
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
   static final String QUERY_LINK_DETERMINISTIC_A_2 =
         """
         query query_link_deterministic_a($national_id: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.national_id,$national_id)) {
               uid
               GoldenRecord.source_id {
                  uid
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
   static final String QUERY_MATCH_DETERMINISTIC_A_2 =
         """
         query query_match_deterministic_a($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.given_name, $given_name)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.family_name, $family_name)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.phone_nummber, $phone_number)) {
               C as uid
            }
            all(func:type(GoldenRecord)) @filter(uid(A) AND uid(B) AND uid(C)) {
               uid
               GoldenRecord.source_id {
                  uid
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
   static final String QUERY_MATCH_PROBABILISTIC_BLOCK_2 =
         """
         query query_match_probabilistic_block($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.given_name, $given_name,3)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.family_name, $family_name,3)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.phone_number, $phone_number,3)) {
               C as uid
            }
            all(func:type(GoldenRecord)) @filter((uid(A) AND uid(B)) OR (uid(A) AND uid(C)) OR (uid(B) AND uid(C))) {
               uid
               GoldenRecord.source_id {
                  uid
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

   private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

   static final String CONFIG_FILE_11 =
         "src%stest%sresources%s%s".formatted(SEPARATOR, SEPARATOR, SEPARATOR, "config-reference-link-dp.json");

   static final String CONFIG_FILE_12 =
         "src%stest%sresources%s%s".formatted(SEPARATOR,
                                              SEPARATOR,
                                              SEPARATOR,
                                              "config-reference-link-d-validate-dp-match-dp.json");


   private TestConstants() {
   }

}
