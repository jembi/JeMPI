package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.DemographicData;

import java.util.UUID;

record NodeInteraction(
      NodeType type,
      UUID uid,
      InteractionData data) implements Node {

   NodeInteraction(final DemographicData demographicData) {
      this(NodeType.INTERACTION, null, new InteractionData(demographicData));
   }

   public NodeType getType() {
      return type;
   }

   public NodeData getNodeData() {
      return data;
   }

}
