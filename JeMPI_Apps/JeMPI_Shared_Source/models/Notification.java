package org.jembi.jempi.shared.models;

import java.util.List;

public record Notification(Long timeStamp,
                           NotificationType notificationType,
                           String dID,   // Document ID
                           String patientName,
                           LinkData linkedTo,
                           List<LinkData> candidates) {
    public enum NotificationType {
        THRESHOLD("Threshold"),
        MARGIN("Margin");

        public final String label;

        private NotificationType(String label) {
            this.label = label;
        }
    }

    public record LinkData(String gID,  // Golden ID
                           Float score) {
    }

}