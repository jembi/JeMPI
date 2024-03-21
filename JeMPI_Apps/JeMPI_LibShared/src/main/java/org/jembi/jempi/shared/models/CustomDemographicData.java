package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CustomDemographicData {

    public static final int GIVEN_NAME = 0;
    public static final int FAMILY_NAME = 1;
    public static final int GENDER = 2;
    public static final int DOB = 3;
    public static final int CITY = 4;
    public static final int PHONE_NUMBER = 5;
    public static final int NATIONAL_ID = 6;

    private CustomDemographicData() {
    }
    
    public static DemographicData fromCustomDemographicFields(
        final String givenName,
        final String familyName,
        final String gender,
        final String dob,
        final String city,
        final String phoneNumber,
        final String nationalId) {
      return new DemographicData(new ArrayList<>(Arrays.asList(
            new DemographicData.Field("givenName", givenName),
            new DemographicData.Field("familyName", familyName),
            new DemographicData.Field("gender", gender),
            new DemographicData.Field("dob", dob),
            new DemographicData.Field("city", city),
            new DemographicData.Field("phoneNumber", phoneNumber),
            new DemographicData.Field("nationalId", nationalId))));
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
