package org.jembi.jempi.libmpi.dgraph;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;
import java.util.Map;

import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;
import static org.jembi.jempi.shared.models.CustomDemographicData.*;

import static org.jembi.jempi.libmpi.dgraph.DgraphQueries.runGoldenRecordsQuery;

final class CustomDgraphQueries {

   static final String QUERY_LINK_DETERMINISTIC_A =
         """
         query query_link_deterministic_a($national_id: string) {
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

   static List<GoldenRecord> queryLinkProbabilisticBlock(final DemographicData demographicData) {
      return List.of();
   }

   static final String QUERY_MATCH_DETERMINISTIC_A =
         """
         query query_match_deterministic_a($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_00, $given_name)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_01, $family_name)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.demographic_field_05, $phone_number)) {
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

   static final String QUERY_MATCH_PROBABILISTIC_BLOCK =
         """
         query query_match_probabilistic_block($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_00, $given_name,3)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_01, $family_name,3)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.demographic_field_05, $phone_number,3)) {
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

   static List<GoldenRecord> queryLinkDeterministicA(final DemographicData demographicData) {
      if (!LINKER_CONFIG.canApplyDeterministicLinking(LINKER_CONFIG.deterministicLinkPrograms.getFirst(), demographicData)) {
         return List.of();
      }
      final Map<String, String> map = Map.of("$national_id", demographicData.fields.get(FIELD_IDX_NATIONAL_ID).value());
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_A, map);
   }

   static List<GoldenRecord> queryMatchDeterministicA(final DemographicData demographicData) {
      final var givenName = demographicData.fields.get(FIELD_IDX_GIVEN_NAME).value();
      final var familyName = demographicData.fields.get(FIELD_IDX_FAMILY_NAME).value();
      final var phoneNumber = demographicData.fields.get(FIELD_IDX_PHONE_NUMBER).value();
      if (!LINKER_CONFIG.canApplyDeterministicLinking(LINKER_CONFIG.deterministicLinkPrograms.getFirst(), demographicData)) {
         return List.of();
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$phone_number",
                             StringUtils.isNotBlank(phoneNumber)
                                   ? phoneNumber
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_MATCH_DETERMINISTIC_A, map);
   }

   static List<GoldenRecord> queryMatchProbabilisticBlock(final DemographicData demographicData) {
      final var givenName = demographicData.fields.get(FIELD_IDX_GIVEN_NAME).value();
      final var familyName = demographicData.fields.get(FIELD_IDX_FAMILY_NAME).value();
      final var phoneNumber = demographicData.fields.get(FIELD_IDX_PHONE_NUMBER).value();
      if (!LINKER_CONFIG.canApplyDeterministicLinking(LINKER_CONFIG.deterministicLinkPrograms.getFirst(), demographicData)) {
         return List.of();
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$phone_number",
                             StringUtils.isNotBlank(phoneNumber)
                                   ? phoneNumber
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_MATCH_PROBABILISTIC_BLOCK, map);
   }

   private CustomDgraphQueries() {
   }

}
