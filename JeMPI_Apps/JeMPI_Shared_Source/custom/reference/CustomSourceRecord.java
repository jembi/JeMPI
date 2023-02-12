package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomSourceRecord(
      RecordType recordType,
      String stan,
      // System Trace Audit Number
      String auxId,
      String givenName,
      String familyName,
      String gender,
      String dob,
      String city,
      String phoneNumber,
      String nationalID,
      String dummy1,
      String dummy2,
      String dummy3) {

   public CustomSourceRecord(RecordType recordType) {
      this(recordType,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null);
   }

   public enum RecordType {
      BATCH_START(RecordType.BATCH_START_VALUE),
      BATCH_END(RecordType.BATCH_END_VALUE),
      BATCH_RECORD(RecordType.BATCH_RECORD_VALUE);
      public static final int BATCH_START_VALUE = 1;
      public static final int BATCH_END_VALUE = 2;
      public static final int BATCH_RECORD_VALUE = 3;

      public final int type;

      RecordType(final int type) {
         this.type = type;
      }
   }

}

