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
   public static final String PREDICATE_GOLDEN_RECORD_ENTITY_LIST = "GoldenRecord.entity_list";

   public static final String PREDICATE_ENTITY_AUX_ID = "Entity.aux_id";
   public static final String PREDICATE_ENTITY_GIVEN_NAME = "Entity.given_name";
   public static final String PREDICATE_ENTITY_FAMILY_NAME = "Entity.family_name";
   public static final String PREDICATE_ENTITY_GENDER = "Entity.gender";
   public static final String PREDICATE_ENTITY_DOB = "Entity.dob";
   public static final String PREDICATE_ENTITY_CITY = "Entity.city";
   public static final String PREDICATE_ENTITY_PHONE_NUMBER = "Entity.phone_number";
   public static final String PREDICATE_ENTITY_NATIONAL_ID = "Entity.national_id";

   static final String QUERY_GET_GOLDEN_RECORD_BY_UID =
      """
      query goldenRecordByUid($uid: string) {
         all(func: uid($uid)) {
            uid
            GoldenRecord.source_id {
               uid
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

   static final String QUERY_GET_GOLDEN_RECORD_ENTITIES =
      """
      query expandedGoldenRecord() {
         all(func: uid(%s)) {
            uid
            GoldenRecord.source_id {
               uid
            }
            GoldenRecord.aux_id
            GoldenRecord.given_name
            GoldenRecord.family_name
            GoldenRecord.gender
            GoldenRecord.dob
            GoldenRecord.city
            GoldenRecord.phone_number
            GoldenRecord.national_id
            GoldenRecord.entity_list @facets(score) {
               uid
               Entity.source_id {
                 uid
                 SourceId.facility
                 SourceId.patient
               }
               Entity.aux_id
               Entity.given_name
               Entity.family_name
               Entity.gender
               Entity.dob
               Entity.city
               Entity.phone_number
               Entity.national_id
            }
         }
      }
      """;

   static final String QUERY_GET_ENTITY_BY_UID =
      """
      query entityByUid($uid: string) {
         all(func: uid($uid)) {
            uid
            Entity.source_id {
              uid
              SourceId.facility
              SourceId.patient
            }
            Entity.aux_id
            Entity.given_name
            Entity.family_name
            Entity.gender
            Entity.dob
            Entity.city
            Entity.phone_number
            Entity.national_id
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
         GoldenRecord.entity_list:               [Entity]
         <~Entity.golden_record_list>
      }
      """;
         
   static final String MUTATION_CREATE_ENTITY_TYPE =
      """

      type Entity {
         Entity.source_id:                     SourceId
         Entity.aux_id
         Entity.given_name
         Entity.family_name
         Entity.gender
         Entity.dob
         Entity.city
         Entity.phone_number
         Entity.national_id
         Entity.golden_record_list:            [GoldenRecord]
      }
      """;

   static final String MUTATION_CREATE_SOURCE_ID_FIELDS =
      """
      SourceId.facility:                     string    @index(exact)                      .
      SourceId.patient:                      string    @index(exact)                      .
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
      GoldenRecord.entity_list:              [uid]     @reverse                           .
      """;

   static final String MUTATION_CREATE_ENTITY_FIELDS =
      """
      Entity.source_id:                    uid                                          .
      Entity.aux_id:                       string                                       .
      Entity.given_name:                   string                                       .
      Entity.family_name:                  string    @index(exact,trigram)              .
      Entity.gender:                       string                                       .
      Entity.dob:                          string                                       .
      Entity.city:                         string                                       .
      Entity.phone_number:                 string                                       .
      Entity.national_id:                  string    @index(exact,trigram)              .
      Entity.golden_record_list:           [uid]     @reverse                           .
      """;

}
