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

   public String getAuxId() {
      return auxId;
   }

   public String getGivenName() {
      return givenName;
   }

   public String getFamilyName() {
      return familyName;
   }

   public String getGender() {
      return gender;
   }

   public String getDob() {
      return dob;
   }

   public String getCity() {
      return city;
   }

   public String getPhoneNumber() {
      return phoneNumber;
   }

   public String getNationalId() {
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

