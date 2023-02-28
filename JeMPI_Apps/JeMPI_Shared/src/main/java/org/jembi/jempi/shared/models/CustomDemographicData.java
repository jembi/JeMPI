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

   public String getNames() {
      var names = "";
      if (!StringUtils.isBlank(givenName())) {
         names += givenName();
      }
      if (!StringUtils.isBlank(familyName())) {
         names += " " + familyName();
      }
      return names;
   }

}
