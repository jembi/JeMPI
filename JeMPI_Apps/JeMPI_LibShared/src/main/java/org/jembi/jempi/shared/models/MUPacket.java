package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MUPacket(
      String tag,
      List<MUPacket.Probability> linkMuPacket,
      List<MUPacket.Probability> validateMuPacket,
      List<MUPacket.Probability> matchMuPacket) {

   public static final int LINK_MU_FIELD_COUNT;
   public static final int VALIDATE_MU_FIELD_COUNT;
   public static final int MATCH_MU_FIELD_COUNT;
   public static final Boolean SEND_INTERACTIONS_TO_EM;

   static {
      LINK_MU_FIELD_COUNT = LINKER_CONFIG.probabilisticLinkFields.size();
      VALIDATE_MU_FIELD_COUNT = LINKER_CONFIG.probabilisticValidateFields.size();
      MATCH_MU_FIELD_COUNT = LINKER_CONFIG.probabilisticMatchNotificationFields.size();
      SEND_INTERACTIONS_TO_EM = LINK_MU_FIELD_COUNT > 0 || VALIDATE_MU_FIELD_COUNT > 0 || MATCH_MU_FIELD_COUNT > 0;
   }

   public record Probability(
         float m,
         float u) {
   }

}
