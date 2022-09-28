DEPRECATED

package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomEntity;

import java.util.List;

import static org.jembi.jempi.libmpi.dgraph.CustomLibMPIConstants.*;

public final class CustomBackEnd {

   private CustomBackEnd() {}

/*
   static void updateGoldenRecordFields(final LibMPI libMPI, final String uid) {
      final var expandedGoldenRecord = libMPI.getGoldenRecordDocuments(List.of(uid)).get(0);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_GIVEN_NAME,
                                      expandedGoldenRecord.entity().givenName(), CustomEntity::givenName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_FAMILY_NAME,
                                      expandedGoldenRecord.entity().familyName(), CustomEntity::familyName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_GENDER,
                                      expandedGoldenRecord.entity().gender(), CustomEntity::gender);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_DOB,
                                      expandedGoldenRecord.entity().dob(), CustomEntity::dob);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_CITY,
                                      expandedGoldenRecord.entity().city(), CustomEntity::city);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_PHONE_NUMBER,
                                      expandedGoldenRecord.entity().phoneNumber(), CustomEntity::phoneNumber);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_NATIONAL_ID,
                                      expandedGoldenRecord.entity().nationalId(), CustomEntity::nationalId);
   }
*/

}
