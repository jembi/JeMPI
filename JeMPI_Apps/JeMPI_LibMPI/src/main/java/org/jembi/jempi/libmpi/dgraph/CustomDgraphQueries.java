package org.jembi.jempi.libmpi.dgraph;

import io.vavr.Function1;
import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jembi.jempi.libmpi.dgraph.DgraphQueries.runGoldenRecordsQuery;

final class CustomDgraphQueries {

   static final List<Function1<CustomDemographicData, DgraphGoldenRecords>> DETERMINISTIC_LINK_FUNCTIONS =
      List.of(CustomDgraphQueries::queryLinkDeterministicE,
              CustomDgraphQueries::queryLinkDeterministicD,
              CustomDgraphQueries::queryLinkDeterministicB,
              CustomDgraphQueries::queryLinkDeterministicA,
              CustomDgraphQueries::queryLinkDeterministicC);

   static final List<Function1<CustomDemographicData, DgraphGoldenRecords>> DETERMINISTIC_MATCH_FUNCTIONS =
      List.of();

   private static final String QUERY_LINK_DETERMINISTIC_E =
         """
         query query_link_deterministic_e($given_name: string, $family_name: string, $phone_number_mobile: string, $phone_number_home: string) {
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.given_name, $given_name)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.family_name, $family_name)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.phone_number_mobile, $phone_number_mobile)) {
               C as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.phone_number_home, $phone_number_home)) {
               D as uid
            }
            all(func:type(GoldenRecord)) @filter(uid(A) AND uid(B) AND (uid(C) OR uid(D))) {
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

   private static final String QUERY_LINK_DETERMINISTIC_D =
         """
         query query_link_deterministic_d($scn: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.scn, $scn)) {
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

   private static final String QUERY_LINK_DETERMINISTIC_B =
         """
         query query_link_deterministic_b($nic: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.nic, $nic)) {
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

   private static final String QUERY_LINK_DETERMINISTIC_A =
         """
         query query_link_deterministic_a($phn: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.phn, $phn)) {
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

   private static final String QUERY_LINK_DETERMINISTIC_C =
         """
         query query_link_deterministic_c($ppn: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.ppn, $ppn)) {
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

   private static DgraphGoldenRecords queryLinkDeterministicE(final CustomDemographicData demographicData) {
      final var givenName = demographicData.givenName;
      final var familyName = demographicData.familyName;
      final var phoneNumberMobile = demographicData.phoneNumberMobile;
      final var phoneNumberHome = demographicData.phoneNumberHome;
      final var givenNameIsBlank = StringUtils.isBlank(givenName);
      final var familyNameIsBlank = StringUtils.isBlank(familyName);
      final var phoneNumberMobileIsBlank = StringUtils.isBlank(phoneNumberMobile);
      final var phoneNumberHomeIsBlank = StringUtils.isBlank(phoneNumberHome);
      if ((givenNameIsBlank || familyNameIsBlank || (phoneNumberMobileIsBlank && phoneNumberHomeIsBlank))) {
         return new DgraphGoldenRecords(List.of());
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$phone_number_mobile",
                             StringUtils.isNotBlank(phoneNumberMobile)
                                   ? phoneNumberMobile
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$phone_number_home",
                             StringUtils.isNotBlank(phoneNumberHome)
                                   ? phoneNumberHome
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_E, map);
   }

   private static DgraphGoldenRecords queryLinkDeterministicD(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.scn)) {
         return new DgraphGoldenRecords(List.of());
      }
      final Map<String, String> map = Map.of("$scn", demographicData.scn);
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_D, map);
   }

   private static DgraphGoldenRecords queryLinkDeterministicB(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.nic)) {
         return new DgraphGoldenRecords(List.of());
      }
      final Map<String, String> map = Map.of("$nic", demographicData.nic);
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_B, map);
   }

   private static DgraphGoldenRecords queryLinkDeterministicA(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.phn)) {
         return new DgraphGoldenRecords(List.of());
      }
      final Map<String, String> map = Map.of("$phn", demographicData.phn);
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_A, map);
   }

   private static DgraphGoldenRecords queryLinkDeterministicC(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.ppn)) {
         return new DgraphGoldenRecords(List.of());
      }
      final Map<String, String> map = Map.of("$ppn", demographicData.ppn);
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_C, map);
   }

   private static void mergeCandidates(
         final List<CustomDgraphGoldenRecord> goldenRecords,
         final DgraphGoldenRecords block) {
      final var candidates = block.all();
      if (!candidates.isEmpty()) {
         candidates.forEach(candidate -> {
            var found = false;
            for (CustomDgraphGoldenRecord goldenRecord : goldenRecords) {
               if (candidate.goldenId().equals(goldenRecord.goldenId())) {
                  found = true;
                  break;
               }
            }
            if (!found) {
               goldenRecords.add(candidate);
            }
         });
      }
   }

   static List<CustomDgraphGoldenRecord> findLinkCandidates(
      final CustomDemographicData interaction) {
      var result = DgraphQueries.deterministicFilter(DETERMINISTIC_LINK_FUNCTIONS, interaction);
      if (!result.isEmpty()) {
         return result;
      }
      result = new LinkedList<>();
      return result;
   }

   static List<CustomDgraphGoldenRecord> findMatchCandidates(
      final CustomDemographicData interaction) {
      var result = DgraphQueries.deterministicFilter(DETERMINISTIC_MATCH_FUNCTIONS, interaction);
      if (!result.isEmpty()) {
         return result;
      }
      result = new LinkedList<>();
      return result;
   }

   private CustomDgraphQueries() {
   }

}
