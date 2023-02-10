package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomPatient;

import java.util.List;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {}

   static void updateGoldenRecordFields(final LibMPI libMPI, final String uid) {
      final var expandedGoldenRecord = libMPI.getMpiExpandedGoldenRecordList(List.of(uid)).get(0);

      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "givenName",
                                      expandedGoldenRecord.customGoldenRecord().givenName(), CustomPatient::givenName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "familyName",
                                      expandedGoldenRecord.customGoldenRecord().familyName(), CustomPatient::familyName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "gender",
                                      expandedGoldenRecord.customGoldenRecord().gender(), CustomPatient::gender);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "dob",
                                      expandedGoldenRecord.customGoldenRecord().dob(), CustomPatient::dob);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "city",
                                      expandedGoldenRecord.customGoldenRecord().city(), CustomPatient::city);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "phoneNumber",
                                      expandedGoldenRecord.customGoldenRecord().phoneNumber(), CustomPatient::phoneNumber);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "nationalId",
                                      expandedGoldenRecord.customGoldenRecord().nationalId(), CustomPatient::nationalId);

   }

}
