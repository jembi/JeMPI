package org.jembi.jempi.shared.models;

public record NotificationAuditEventData(
      String message,
      String notificationId
) {
}
