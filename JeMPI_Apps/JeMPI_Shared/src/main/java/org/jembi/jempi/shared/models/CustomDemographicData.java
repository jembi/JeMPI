package org.jembi.jempi.shared.models;

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

}

