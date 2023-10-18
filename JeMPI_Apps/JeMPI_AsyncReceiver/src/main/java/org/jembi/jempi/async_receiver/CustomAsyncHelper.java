package org.jembi.jempi.async_receiver;

import org.apache.commons.csv.CSVRecord;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomSourceId;
import org.jembi.jempi.shared.models.CustomUniqueInteractionData;

final class CustomAsyncHelper {

   private static final int AUX_ID_COL_NUM = 0;
   private static final int AUX_CLINICAL_DATA_COL_NUM = 15;
   private static final int SOURCEID_FACILITY_COL_NUM = 13;
   private static final int SOURCEID_PATIENT_COL_NUM = 14;
   private static final int GIVEN_NAME_COL_NUM = 1;
   private static final int FAMILY_NAME_COL_NUM = 2;
   private static final int GENDER_COL_NUM = 3;
   private static final int DOB_COL_NUM = 4;
   private static final int CITY_COL_NUM = 5;
   private static final int PHONE_NUMBER_HOME_COL_NUM = 6;
   private static final int PHONE_NUMBER_MOBILE_COL_NUM = 7;
   private static final int PHN_COL_NUM = 8;
   private static final int NIC_COL_NUM = 9;
   private static final int PPN_COL_NUM = 10;
   private static final int SCN_COL_NUM = 11;
   private static final int DL_COL_NUM = 12;

   private CustomAsyncHelper() {
   }

   static CustomUniqueInteractionData customUniqueInteractionData(final CSVRecord csvRecord) {
      return new CustomUniqueInteractionData(java.time.LocalDateTime.now(),
                                             Main.parseRecordNumber(csvRecord.get(AUX_ID_COL_NUM)),
                                             csvRecord.get(AUX_CLINICAL_DATA_COL_NUM));
   }

   static CustomDemographicData customDemographicData(final CSVRecord csvRecord) {
      return new CustomDemographicData(
         csvRecord.get(GIVEN_NAME_COL_NUM),
         csvRecord.get(FAMILY_NAME_COL_NUM),
         csvRecord.get(GENDER_COL_NUM),
         csvRecord.get(DOB_COL_NUM),
         csvRecord.get(CITY_COL_NUM),
         csvRecord.get(PHONE_NUMBER_HOME_COL_NUM),
         csvRecord.get(PHONE_NUMBER_MOBILE_COL_NUM),
         csvRecord.get(PHN_COL_NUM),
         csvRecord.get(NIC_COL_NUM),
         csvRecord.get(PPN_COL_NUM),
         csvRecord.get(SCN_COL_NUM),
         csvRecord.get(DL_COL_NUM));
   }

   static CustomSourceId customSourceId(final CSVRecord csvRecord) {
      return new CustomSourceId(
         null,
         csvRecord.get(SOURCEID_FACILITY_COL_NUM),
         csvRecord.get(SOURCEID_PATIENT_COL_NUM));
   }

}

