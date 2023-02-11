package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomGoldenRecord(String uid,
                                 List<SourceId> sourceId,
                                 String auxId,
                                 String givenName,
                                 String familyName,
                                 String gender,
                                 String dob,
                                 String city,
                                 String phoneNumber,
                                 String nationalId) {

   public CustomGoldenRecord(final CustomPatient patient) {
      this(null,
           List.of(patient.sourceId()),
           patient.auxId(),
           patient.givenName(),
           patient.familyName(),
           patient.gender(),
           patient.dob(),
           patient.city(),
           patient.phoneNumber(),
           patient.nationalId());
   }

}
