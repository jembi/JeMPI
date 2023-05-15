package org.jembi.jempi.libmpi.postgresql;

import java.util.UUID;

record NodeSourceId(
      NodeType type,
      UUID id,
      SourceIdData data) implements Node {

   NodeSourceId(
         final String facility,
         final String patient) {
      this(NodeType.SOURCE_ID, null, new SourceIdData(facility, patient));
   }

   public NodeType getType() {
      return type;
   }

   public NodeData getNodeData() {
      return data;
   }

   record SourceIdData(
         String facility,
         String patient) implements NodeData {
   }

}
