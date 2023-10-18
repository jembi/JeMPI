package org.jembi.jempi.libmpi.dgraph;

public final class CustomDgraphConstants {

   public static final String PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED = "GoldenRecord.aux_date_created";
   public static final String PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED = "GoldenRecord.aux_auto_update_enabled";
   public static final String PREDICATE_GOLDEN_RECORD_AUX_ID = "GoldenRecord.aux_id";
   public static final String PREDICATE_GOLDEN_RECORD_GIVEN_NAME = "GoldenRecord.given_name";
   public static final String PREDICATE_GOLDEN_RECORD_FAMILY_NAME = "GoldenRecord.family_name";
   public static final String PREDICATE_GOLDEN_RECORD_GENDER = "GoldenRecord.gender";
   public static final String PREDICATE_GOLDEN_RECORD_DOB = "GoldenRecord.dob";
   public static final String PREDICATE_GOLDEN_RECORD_CITY = "GoldenRecord.city";
   public static final String PREDICATE_GOLDEN_RECORD_PHONE_NUMBER_HOME = "GoldenRecord.phone_number_home";
   public static final String PREDICATE_GOLDEN_RECORD_PHONE_NUMBER_MOBILE = "GoldenRecord.phone_number_mobile";
   public static final String PREDICATE_GOLDEN_RECORD_PHN = "GoldenRecord.phn";
   public static final String PREDICATE_GOLDEN_RECORD_NIC = "GoldenRecord.nic";
   public static final String PREDICATE_GOLDEN_RECORD_PPN = "GoldenRecord.ppn";
   public static final String PREDICATE_GOLDEN_RECORD_SCN = "GoldenRecord.scn";
   public static final String PREDICATE_GOLDEN_RECORD_DL = "GoldenRecord.dl";
   public static final String PREDICATE_GOLDEN_RECORD_INTERACTIONS = "GoldenRecord.interactions";
   public static final String PREDICATE_INTERACTION_AUX_DATE_CREATED = "Interaction.aux_date_created";
   public static final String PREDICATE_INTERACTION_AUX_ID = "Interaction.aux_id";
   public static final String PREDICATE_INTERACTION_AUX_CLINICAL_DATA = "Interaction.aux_clinical_data";
   public static final String PREDICATE_INTERACTION_GIVEN_NAME = "Interaction.given_name";
   public static final String PREDICATE_INTERACTION_FAMILY_NAME = "Interaction.family_name";
   public static final String PREDICATE_INTERACTION_GENDER = "Interaction.gender";
   public static final String PREDICATE_INTERACTION_DOB = "Interaction.dob";
   public static final String PREDICATE_INTERACTION_CITY = "Interaction.city";
   public static final String PREDICATE_INTERACTION_PHONE_NUMBER_HOME = "Interaction.phone_number_home";
   public static final String PREDICATE_INTERACTION_PHONE_NUMBER_MOBILE = "Interaction.phone_number_mobile";
   public static final String PREDICATE_INTERACTION_PHN = "Interaction.phn";
   public static final String PREDICATE_INTERACTION_NIC = "Interaction.nic";
   public static final String PREDICATE_INTERACTION_PPN = "Interaction.ppn";
   public static final String PREDICATE_INTERACTION_SCN = "Interaction.scn";
   public static final String PREDICATE_INTERACTION_DL = "Interaction.dl";

   static final String GOLDEN_RECORD_FIELD_NAMES =
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
         GoldenRecord.phone_number_home
         GoldenRecord.phone_number_mobile
         GoldenRecord.phn
         GoldenRecord.nic
         GoldenRecord.ppn
         GoldenRecord.scn
         GoldenRecord.dl
         """;

   static final String EXPANDED_GOLDEN_RECORD_FIELD_NAMES =
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
         GoldenRecord.phone_number_home
         GoldenRecord.phone_number_mobile
         GoldenRecord.phn
         GoldenRecord.nic
         GoldenRecord.ppn
         GoldenRecord.scn
         GoldenRecord.dl
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
            Interaction.phone_number_home
            Interaction.phone_number_mobile
            Interaction.phn
            Interaction.nic
            Interaction.ppn
            Interaction.scn
            Interaction.dl
         }
         """;
   static final String INTERACTION_FIELD_NAMES =
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
         Interaction.phone_number_home
         Interaction.phone_number_mobile
         Interaction.phn
         Interaction.nic
         Interaction.ppn
         Interaction.scn
         Interaction.dl
         """;

   static final String EXPANDED_INTERACTION_FIELD_NAMES =
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
         Interaction.phone_number_home
         Interaction.phone_number_mobile
         Interaction.phn
         Interaction.nic
         Interaction.ppn
         Interaction.scn
         Interaction.dl
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
            GoldenRecord.phone_number_home
            GoldenRecord.phone_number_mobile
            GoldenRecord.phn
            GoldenRecord.nic
            GoldenRecord.ppn
            GoldenRecord.scn
            GoldenRecord.dl
         }
         """;

   static final String QUERY_GET_INTERACTION_BY_UID =
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
               Interaction.phone_number_home
               Interaction.phone_number_mobile
               Interaction.phn
               Interaction.nic
               Interaction.ppn
               Interaction.scn
               Interaction.dl
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
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number_home
               GoldenRecord.phone_number_mobile
               GoldenRecord.phn
               GoldenRecord.nic
               GoldenRecord.ppn
               GoldenRecord.scn
               GoldenRecord.dl
            }
         }
         """;

   static final String QUERY_GET_EXPANDED_INTERACTIONS =
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
               Interaction.phone_number_home
               Interaction.phone_number_mobile
               Interaction.phn
               Interaction.nic
               Interaction.ppn
               Interaction.scn
               Interaction.dl
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
                  GoldenRecord.phone_number_home
                  GoldenRecord.phone_number_mobile
                  GoldenRecord.phn
                  GoldenRecord.nic
                  GoldenRecord.ppn
                  GoldenRecord.scn
                  GoldenRecord.dl
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
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number_home
               GoldenRecord.phone_number_mobile
               GoldenRecord.phn
               GoldenRecord.nic
               GoldenRecord.ppn
               GoldenRecord.scn
               GoldenRecord.dl
            }
         }
         """;

   static final String QUERY_GET_EXPANDED_GOLDEN_RECORDS =
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
               GoldenRecord.phone_number_home
               GoldenRecord.phone_number_mobile
               GoldenRecord.phn
               GoldenRecord.nic
               GoldenRecord.ppn
               GoldenRecord.scn
               GoldenRecord.dl
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
                  Interaction.phone_number_home
                  Interaction.phone_number_mobile
                  Interaction.phn
                  Interaction.nic
                  Interaction.ppn
                  Interaction.scn
                  Interaction.dl
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
            GoldenRecord.aux_date_created
            GoldenRecord.aux_auto_update_enabled
            GoldenRecord.aux_id
            GoldenRecord.given_name
            GoldenRecord.family_name
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.city
            GoldenRecord.phone_number_home
            GoldenRecord.phone_number_mobile
            GoldenRecord.phn
            GoldenRecord.nic
            GoldenRecord.ppn
            GoldenRecord.scn
            GoldenRecord.dl
            GoldenRecord.interactions:              [Interaction]
         }
         """;
           
   static final String MUTATION_CREATE_GOLDEN_RECORD_FIELDS =
         """
         GoldenRecord.source_id:                [uid]                                        .
         GoldenRecord.aux_date_created:         datetime                                     .
         GoldenRecord.aux_auto_update_enabled:  bool                                         .
         GoldenRecord.aux_id:                   string                                       .
         GoldenRecord.given_name:               string    @index(exact,trigram)              .
         GoldenRecord.family_name:              string    @index(exact,trigram)              .
         GoldenRecord.gender:                   string    @index(exact)                      .
         GoldenRecord.dob:                      string                                       .
         GoldenRecord.city:                     string    @index(trigram)                    .
         GoldenRecord.phone_number_home:        string    @index(exact,trigram)              .
         GoldenRecord.phone_number_mobile:      string    @index(exact,trigram)              .
         GoldenRecord.phn:                      string    @index(exact,trigram)              .
         GoldenRecord.nic:                      string    @index(exact,trigram)              .
         GoldenRecord.ppn:                      string    @index(exact,trigram)              .
         GoldenRecord.scn:                      string    @index(exact,trigram)              .
         GoldenRecord.dl:                       string    @index(exact,trigram)              .
         GoldenRecord.interactions:             [uid]     @reverse                           .
         """;

   static final String MUTATION_CREATE_INTERACTION_TYPE =
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
            Interaction.phone_number_home
            Interaction.phone_number_mobile
            Interaction.phn
            Interaction.nic
            Interaction.ppn
            Interaction.scn
            Interaction.dl
         }
         """;

   static final String MUTATION_CREATE_INTERACTION_FIELDS =
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
         Interaction.phone_number_home:            string                                       .
         Interaction.phone_number_mobile:          string                                       .
         Interaction.phn:                          string    @index(exact,trigram)              .
         Interaction.nic:                          string    @index(exact,trigram)              .
         Interaction.ppn:                          string    @index(exact,trigram)              .
         Interaction.scn:                          string    @index(exact,trigram)              .
         Interaction.dl:                           string    @index(exact,trigram)              .
         """;

   private CustomDgraphConstants() {}

}
