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

}

