package org.jembi.jempi.shared.models;

public class AsyncReceiverMetadata {
   public String startDateTime;
   public String endDateTime;

   public AsyncReceiverMetadata() { }
   public final void setStartDateTime(final String startDateTime) {
      this.startDateTime = startDateTime;
   }

   public final void setEndDateTime(final String endDateTime) {
      this.endDateTime = endDateTime;
   }
}
