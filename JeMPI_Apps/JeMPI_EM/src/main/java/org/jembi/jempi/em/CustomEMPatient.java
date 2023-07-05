package org.jembi.jempi.em;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.CustomDemographicData;

record CustomEMPatient(
      String col1,
      String col1Phonetic,
      String col2,
      String col2Phonetic,
      String genderAtBirth,
      String dateOfBirth,
      String city,
      String cityPhonetic,
      String phoneNumber,
      String nationalID) {

   CustomEMPatient(final CustomDemographicData patient) {
      this(StringUtils.EMPTY, StringUtils.EMPTY,
           StringUtils.EMPTY, StringUtils.EMPTY,
           StringUtils.EMPTY,
           StringUtils.EMPTY,
           StringUtils.EMPTY, StringUtils.EMPTY, // patient.city, CustomEMTask.getPhonetic(patient.city),
           StringUtils.EMPTY, // patient.phoneNumber,
           null);
   }
}


