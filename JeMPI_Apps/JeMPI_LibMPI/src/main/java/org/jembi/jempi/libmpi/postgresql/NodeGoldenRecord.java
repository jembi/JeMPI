package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.UUID;

record NodeGoldenRecord(
      NodeType type,
      UUID uid,
      GoldenRecordData data) implements Node {

   NodeGoldenRecord(final CustomDemographicData demographicData) {
      this(NodeType.GOLDEN_RECORD, null, new GoldenRecordData(demographicData));
   }

   public NodeType getType() {
      return type;
   }

   public NodeData getNodeData() {
      return data;
   }

   public static class GoldenRecordData extends CustomDemographicData implements NodeData {

      GoldenRecordData(
            final String auxId,
            final String givenName,
            final String familyName,
            final String gender,
            final String dob,
            final String city,
            final String phoneNumber,
            final String nationalId) {
         super(auxId, givenName, familyName, gender, dob, city, phoneNumber, nationalId);
      }

      GoldenRecordData(final CustomDemographicData customDemographicData) {
         super(customDemographicData.auxId,
               customDemographicData.givenName,
               customDemographicData.familyName,
               customDemographicData.gender,
               customDemographicData.dob,
               customDemographicData.city,
               customDemographicData.phoneNumber,
               customDemographicData.nationalId);
      }

   }

}
