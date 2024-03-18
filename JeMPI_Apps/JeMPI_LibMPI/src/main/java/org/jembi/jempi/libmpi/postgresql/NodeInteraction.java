package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.UUID;

record NodeInteraction(
      NodeType type,
      UUID uid,
      InteractionData data) implements Node {

   NodeInteraction(final CustomDemographicData demographicData) {
      this(NodeType.INTERACTION, null, new InteractionData(demographicData));
   }

   public NodeType getType() {
      return type;
   }

   public NodeData getNodeData() {
      return data;
   }

}
