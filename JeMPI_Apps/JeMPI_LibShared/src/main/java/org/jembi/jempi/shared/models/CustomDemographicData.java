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
   public final String phn;
   public final String nic;
   public final String myGoldenIdA;
   public final String myGoldenIdB;

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

   public final String getPhn() {
      return phn;
   }

   public final String getNic() {
      return nic;
   }

   public final String getMyGoldenIdA() {
      return myGoldenIdA;
   }

   public final String getMyGoldenIdB() {
      return myGoldenIdB;
   }

   public CustomDemographicData() {
      this(null, null, null, null, null, null, null, null, null, null);
   }

   public CustomDemographicData(
      final String givenName,
      final String familyName,
      final String gender,
      final String dob,
      final String city,
      final String phoneNumber,
      final String phn,
      final String nic,
      final String myGoldenIdA,
      final String myGoldenIdB) {
         this.givenName = givenName;
         this.familyName = familyName;
         this.gender = gender;
         this.dob = dob;
         this.city = city;
         this.phoneNumber = phoneNumber;
         this.phn = phn;
         this.nic = nic;
         this.myGoldenIdA = myGoldenIdA;
         this.myGoldenIdB = myGoldenIdB;
   }

   public CustomDemographicData clean() {
      return new CustomDemographicData(this.givenName.toLowerCase().replaceAll("\\W", ""),
                                       this.familyName.toLowerCase().replaceAll("\\W", ""),
                                       this.gender.toLowerCase().replaceAll("\\W", ""),
                                       this.dob.toLowerCase().replaceAll("\\W", ""),
                                       this.city.toLowerCase().replaceAll("\\W", ""),
                                       this.phoneNumber.toLowerCase().replaceAll("\\W", ""),
                                       this.phn.toLowerCase().replaceAll("\\W", ""),
                                       this.nic.toLowerCase().replaceAll("\\W", ""),
                                       this.myGoldenIdA.toLowerCase().replaceAll("\\W", ""),
                                       this.myGoldenIdB.toLowerCase().replaceAll("\\W", ""));
   }

}
