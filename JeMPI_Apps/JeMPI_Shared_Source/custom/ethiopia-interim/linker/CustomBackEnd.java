package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomEntity;

import java.util.List;

import static org.jembi.jempi.libmpi.dgraph.CustomLibMPIConstants.*;

public final class CustomBackEnd {

   private CustomBackEnd() {}

   static void updateGoldenRecordFields(final LibMPI libMPI, final String uid) {
      final var expandedGoldenRecordList = libMPI.getGoldenRecordDocuments(List.of(uid));
      final var expandedGoldenRecord = expandedGoldenRecordList.get(0);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_NAME_GIVEN,
                                      expandedGoldenRecord.entity().nameGiven(), CustomEntity::nameGiven);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_NAME_FATHER,
                                      expandedGoldenRecord.entity().nameFather(), CustomEntity::nameFather);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_NAME_FATHERS_FATHER,
                                      expandedGoldenRecord.entity().nameFathersFather(), CustomEntity::nameFathersFather);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_NAME_MOTHER,
                                      expandedGoldenRecord.entity().nameMother(), CustomEntity::nameMother);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_NAME_MOTHERS_FATHER,
                                      expandedGoldenRecord.entity().nameMothersFather(), CustomEntity::nameMothersFather);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_GENDER,
                                      expandedGoldenRecord.entity().gender(), CustomEntity::gender);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_DOB,
                                      expandedGoldenRecord.entity().dob(), CustomEntity::dob);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_CITY,
                                      expandedGoldenRecord.entity().city(), CustomEntity::city);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord, PREDICATE_GOLDEN_RECORD_PHONE_NUMBER,
                                      expandedGoldenRecord.entity().phoneNumber(), CustomEntity::phoneNumber);
   }

}
