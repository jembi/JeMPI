package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomDemographicData {
   public final String auxId;
   public final String givenName;
   public final String familyName;
   public final String gender;
   public final String dob;
   public final String city;
   public final String phoneNumber;
   public final String nationalId;

   public final String getAuxId() {
      return auxId;
   }

   public final String getGivenName() {
      return givenName;
   }

   public final String getFamilyName() {
      return familyName;
   }

   public final String getGender() {
      return gender;
   }

   public final String getDob() {
      return dob;
   }

   public final String getCity() {
      return city;
   }

   public final String getPhoneNumber() {
      return phoneNumber;
   }

   public final String getNationalId() {
      return nationalId;
   }

   public CustomDemographicData() {
      this(null, null, null, null, null, null, null, null);
   }

   public CustomDemographicData(
      final String auxId,
      final String givenName,
      final String familyName,
      final String gender,
      final String dob,
      final String city,
      final String phoneNumber,
      final String nationalId) {
         this.auxId = auxId;
         this.givenName = givenName;
         this.familyName = familyName;
         this.gender = gender;
         this.dob = dob;
         this.city = city;
         this.phoneNumber = phoneNumber;
         this.nationalId = nationalId;
   }

}
