package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomPatient(
      String uid,
      SourceId sourceId,
      CustomDemographicData demographicData) {
   public static String getNames(final CustomPatient patient) {
      return ((StringUtils.isBlank(patient.demographicData.givenName())
                     ? ""
                     : " " + patient.demographicData.givenName()) + (StringUtils.isBlank(patient.demographicData.familyName())
                                                                           ? ""
                                                                           : " " + patient.demographicData.familyName())).trim();
   }

}
