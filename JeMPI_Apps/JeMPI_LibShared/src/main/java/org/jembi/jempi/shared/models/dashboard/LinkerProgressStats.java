package org.jembi.jempi.shared.models.dashboard;

public record LinkerProgressStats(long totalCompleted, long sizeCompleted, long toFileSize, String filename) {
}
