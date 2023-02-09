package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomEntity;

import java.util.List;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {}

   static void updateGoldenRecordFields(final LibMPI libMPI, final String uid) {
      final var expandedGoldenRecord = libMPI.getMpiExpandedGoldenRecordList(List.of(uid)).get(0);

      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "givenName",
                                      expandedGoldenRecord.customGoldenRecord().givenName(), CustomEntity::givenName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "familyName",
                                      expandedGoldenRecord.customGoldenRecord().familyName(), CustomEntity::familyName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "gender",
                                      expandedGoldenRecord.customGoldenRecord().gender(), CustomEntity::gender);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "dob",
                                      expandedGoldenRecord.customGoldenRecord().dob(), CustomEntity::dob);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "city",
                                      expandedGoldenRecord.customGoldenRecord().city(), CustomEntity::city);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "phoneNumber",
                                      expandedGoldenRecord.customGoldenRecord().phoneNumber(), CustomEntity::phoneNumber);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "nationalId",
                                      expandedGoldenRecord.customGoldenRecord().nationalId(), CustomEntity::nationalId);

   }

}
