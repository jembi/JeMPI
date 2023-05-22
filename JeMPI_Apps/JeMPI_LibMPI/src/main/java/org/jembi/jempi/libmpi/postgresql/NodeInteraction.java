package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.UUID;

record NodeEncounter(
      NodeType type,
      UUID uid,
      CustomInteractionData data) implements Node {

   NodeEncounter(final CustomDemographicData demographicData) {
      this(NodeType.ENCOUNTER, null, new CustomInteractionData(demographicData));
   }

   public NodeType getType() {
      return type;
   }

   public NodeData getNodeData() {
      return data;
   }

/*
   public static class EncounterData extends CustomDemographicData implements NodeData {
      EncounterData(
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

      EncounterData(final CustomDemographicData customDemographicData) {
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
*/

}
