package org.jembi.jempi.shared.models;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomDemographicData(
      String auxId,
      String givenName,
      String familyName,
      String gender,
      String dob,
      String city,
      String phoneNumber,
      String nationalId) {

   public static String getNames(final CustomDemographicData demographicData) {
      return ((StringUtils.isBlank(demographicData.givenName())
                     ? ""
                     : " " + demographicData.givenName()) +
              (StringUtils.isBlank(demographicData.familyName())
                     ? ""
                     : " " + demographicData.familyName())).trim();
   }

}
