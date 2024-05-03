package org.jembi.jempi.shared.models;

public class LinkerMetadata {
   public String startDateTime;
   public String endDateTime;

   public LinkerMetadata() { }

   public LinkerMetadata(final String startDateTime, final String endDateTime) {
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
   }

   public final void setStartDateTime(final String startDateTime) {
      this.startDateTime = startDateTime;
   }

   public final void setEndDateTime(final String endDateTime) {
      this.endDateTime = endDateTime;
   }
}
