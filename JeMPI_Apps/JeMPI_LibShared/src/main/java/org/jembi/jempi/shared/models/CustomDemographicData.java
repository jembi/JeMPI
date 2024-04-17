package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomDemographicData {
   public final String givenName;
   public final String familyName;
   public final String gender;
   public final String dob;
   public final String city;
   public final String phoneNumber;
   public final String nationalId;

   public CustomDemographicData() {
      this(null, null, null, null, null, null, null);
   }

   public CustomDemographicData(final CustomDemographicData demographicData) {
      this.givenName = demographicData.givenName;
      this.familyName = demographicData.familyName;
      this.gender = demographicData.gender;
      this.dob = demographicData.dob;
      this.city = demographicData.city;
      this.phoneNumber = demographicData.phoneNumber;
      this.nationalId = demographicData.nationalId;
   }

   public CustomDemographicData(
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

   public CustomDemographicData clean() {
      return new CustomDemographicData(this.givenName.trim().toLowerCase().replaceAll("\\W", ""),
                                       this.familyName.trim().toLowerCase().replaceAll("\\W", ""),
                                       this.gender.trim().toLowerCase().replaceAll("\\W", ""),
                                       this.dob.trim().toLowerCase().replaceAll("\\W", ""),
                                       this.city.trim().toLowerCase().replaceAll("\\W", ""),
                                       this.phoneNumber.trim().toLowerCase().replaceAll("\\W", ""),
                                       this.nationalId.trim().toLowerCase().replaceAll("\\W", ""));
   }

}
