package org.jembi.jempi.shared.models;


public record NotificationResolutionProcessorData(
        NotificationResolution notificationResolution,
        LinkInfo linkInfo
        ) {
}
