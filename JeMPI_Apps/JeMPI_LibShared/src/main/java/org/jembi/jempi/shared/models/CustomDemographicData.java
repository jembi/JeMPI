package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomDemographicData {
   public final String givenName;
   public final String familyName;
   public final String gender;
   public final String dob;
   public final String city;
   public final String phoneNumberHome;
   public final String phoneNumberMobile;
   public final String phn;
   public final String nic;
   public final String ppn;
   public final String scn;
   public final String dl;

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

   public final String getPhoneNumberHome() {
      return phoneNumberHome;
   }

   public final String getPhoneNumberMobile() {
      return phoneNumberMobile;
   }

   public final String getPhn() {
      return phn;
   }

   public final String getNic() {
      return nic;
   }

   public final String getPpn() {
      return ppn;
   }

   public final String getScn() {
      return scn;
   }

   public final String getDl() {
      return dl;
   }

   public CustomDemographicData() {
      this(null, null, null, null, null, null, null, null, null, null, null, null);
   }

   public CustomDemographicData(
      final String givenName,
      final String familyName,
      final String gender,
      final String dob,
      final String city,
      final String phoneNumberHome,
      final String phoneNumberMobile,
      final String phn,
      final String nic,
      final String ppn,
      final String scn,
      final String dl) {
         this.givenName = givenName;
         this.familyName = familyName;
         this.gender = gender;
         this.dob = dob;
         this.city = city;
         this.phoneNumberHome = phoneNumberHome;
         this.phoneNumberMobile = phoneNumberMobile;
         this.phn = phn;
         this.nic = nic;
         this.ppn = ppn;
         this.scn = scn;
         this.dl = dl;
   }

   public CustomDemographicData clean() {
      return new CustomDemographicData(this.givenName.toLowerCase().replaceAll("\\W", ""),
                                       this.familyName.toLowerCase().replaceAll("\\W", ""),
                                       this.gender.toLowerCase().replaceAll("\\W", ""),
                                       this.dob.toLowerCase().replaceAll("\\W", ""),
                                       this.city.toLowerCase().replaceAll("\\W", ""),
                                       this.phoneNumberHome.toLowerCase().replaceAll("\\W", ""),
                                       this.phoneNumberMobile.toLowerCase().replaceAll("\\W", ""),
                                       this.phn.toLowerCase().replaceAll("\\W", ""),
                                       this.nic.toLowerCase().replaceAll("\\W", ""),
                                       this.ppn.toLowerCase().replaceAll("\\W", ""),
                                       this.scn.toLowerCase().replaceAll("\\W", ""),
                                       this.dl.toLowerCase().replaceAll("\\W", ""));
   }

}
