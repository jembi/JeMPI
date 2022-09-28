package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomEntity;

import java.util.List;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {}

   static void updateGoldenRecordFields(final LibMPI libMPI, final String uid) {
      final var expandedGoldenRecord = libMPI.getMpiExpandedGoldenRecordList(List.of(uid)).get(0);

      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.given_name",
                                      expandedGoldenRecord.customGoldenRecord().givenName(), CustomEntity::givenName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.family_name",
                                      expandedGoldenRecord.customGoldenRecord().familyName(), CustomEntity::familyName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.gender",
                                      expandedGoldenRecord.customGoldenRecord().gender(), CustomEntity::gender);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.dob",
                                      expandedGoldenRecord.customGoldenRecord().dob(), CustomEntity::dob);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.city",
                                      expandedGoldenRecord.customGoldenRecord().city(), CustomEntity::city);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.phone_number",
                                      expandedGoldenRecord.customGoldenRecord().phoneNumber(), CustomEntity::phoneNumber);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, "GoldenRecord.national_id",
                                      expandedGoldenRecord.customGoldenRecord().nationalId(), CustomEntity::nationalId);

   }

}
