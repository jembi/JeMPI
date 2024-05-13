package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CustomDemographicData {

   private CustomDemographicData() {
   }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomDemographicDataAPI {
        public final String givenName;
        public final String familyName;
        public final String gender;
        public final String dob;
        public final String city;
        public final String phoneNumber;
        public final String nationalId;

      public CustomDemographicDataAPI(
            final String givenName,
            final String familyName,
            final String gender,
            final String dob,
            final String city,
            final String phoneNumber,
            final String nationalId) {
         this.givenName = givenName;
         this.familyName = familyName;
         this.gender = gender;
         this.dob = dob;
         this.city = city;
         this.phoneNumber = phoneNumber;
         this.nationalId = nationalId;
      }

      public CustomDemographicDataAPI() {
         this(null, null, null, null, null, null, null);
      }

   }

}
