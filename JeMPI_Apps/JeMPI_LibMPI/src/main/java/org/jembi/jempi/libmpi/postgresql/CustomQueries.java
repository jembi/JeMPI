package org.jembi.jempi.libmpi.postgresql;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.CustomDemographicData;

final class CustomQueries {

   private CustomQueries() {
   }

   static String sqlDeterministicCandidates(final CustomDemographicData customDemographicData) {
      return StringUtils.EMPTY;
/*
      return String.format(
            """
            SELECT * FROM %s
            WHERE (fields->>'nationalId' = '') IS FALSE AND fields @> '{"nationalId": "%s"}'
               OR ((fields->>'givenName' = '') IS FALSE AND fields @> '{"givenName":"%s"}' AND
                   (fields->>'familyName' = '') IS FALSE AND fields @> '{"familyName":"%s"}' AND
                   (fields->>'phoneNumber' = '') IS FALSE AND fields @> '{"phoneNumber":"%s"}');
            """,
            TABLE_NODE_GOLDEN_RECORDS,
            customDemographicData.phoneNumber,
            customDemographicData.givenName,
            customDemographicData.familyName,
            customDemographicData.phoneNumber).stripIndent();
*/
   }

   static String sqlBlockedCandidates(final CustomDemographicData customDemographicData) {
      return StringUtils.EMPTY;
/*
      return String.format(
            """
            SELECT * FROM %s
            WHERE ((fields->>'givenName')   %% '%s' AND (fields->>'familyName') %% '%s')
               OR ((fields->>'givenName')   %% '%s' AND (fields->>'city')       %% '%s')
               OR ((fields->>'familyName')  %% '%s' AND (fields->>'city')       %% '%s')
               OR ((fields->>'phoneNumber') %% '%s')
               OR ((fields->>'nationalId')  %% '%s');
            """,
            TABLE_NODE_GOLDEN_RECORDS,
            customDemographicData.givenName,
            customDemographicData.familyName,
            customDemographicData.givenName,
            customDemographicData.city,
            customDemographicData.familyName,
            customDemographicData.city,
            customDemographicData.phoneNumber,
            customDemographicData.phoneNumber).stripIndent();
*/
   }

}
