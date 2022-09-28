package org.jembi.jempi.shared.models;

public record JournalEntry (
      String stan,
      Long timeStamp,
      String message
) {}