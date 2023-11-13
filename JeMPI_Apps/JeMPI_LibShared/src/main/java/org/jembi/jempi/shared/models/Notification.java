package org.jembi.jempi.shared.models;

import java.util.List;

public record Notification(
      Long timeStamp,
      // UTC Time
      NotificationType notificationType,
      String dID,
      // Document ID
      String patientNames,
      MatchData linkedTo,
      List<MatchData> candidates) {
   public enum NotificationType {
      THRESHOLD("Threshold"),
      MARGIN("Margin"),
      UPDATE("Update");

      public final String label;

      NotificationType(final String label) {
         this.label = label;
      }
   }

   public enum NotificationState {
      OPEN("OPEN"),
      CLOSED("CLOSED");
      public final String label;

      NotificationState(final String label) {
         this.label = label;
      }
   }

   public record MatchData(
         String gID,
         // Golden ID
         Float score) {
   }
}
