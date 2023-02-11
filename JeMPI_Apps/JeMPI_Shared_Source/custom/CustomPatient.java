package org.jembi.jempi.shared.models;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomPatient(String uid,
                            SourceId sourceId,
                            String auxId,
                            String givenName,
                            String familyName,
                            String gender,
                            String dob,
                            String city,
                            String phoneNumber,
                            String nationalId) {
   public static String getNames(final CustomPatient patient) {
      return ((StringUtils.isBlank(patient.givenName) ? "" : " " + patient.givenName) + 
              (StringUtils.isBlank(patient.familyName) ? "" : " " + patient.familyName)).trim();
   }

}
