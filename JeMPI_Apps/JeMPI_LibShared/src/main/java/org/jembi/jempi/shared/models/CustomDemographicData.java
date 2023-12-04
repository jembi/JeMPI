package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomDemographicData {
   public final String givenName;
   public final String familyName;
   public final String gender;
   public final String dob;
   public final String city;
   public final String phoneNumber;
   public final String nationalId;

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
      this(null, null, null, null, null, null, null);
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
      return new CustomDemographicData(this.givenName.toLowerCase().replaceAll("\\W", ""),
                                       this.familyName.toLowerCase().replaceAll("\\W", ""),
                                       this.gender.toLowerCase().replaceAll("\\W", ""),
                                       this.dob.toLowerCase().replaceAll("\\W", ""),
                                       this.city.toLowerCase().replaceAll("\\W", ""),
                                       this.phoneNumber.toLowerCase().replaceAll("\\W", ""),
                                       this.nationalId.toLowerCase().replaceAll("\\W", ""));
   }

   public Map<String, String> toMap(){
      return Map.ofEntries(
              Map.entry("givenName", this.familyName),
              Map.entry("familyName", this.familyName),
              Map.entry("gender", this.familyName),
              Map.entry("dob", this.familyName),
              Map.entry("city", this.familyName),
              Map.entry("phoneNumber", this.familyName),
              Map.entry("nationalId", this.familyName)
      );
   }

}
