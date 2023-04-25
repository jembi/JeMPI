package org.jembi.jempi.shared.models;

public record AsyncSourceRecord(
      RecordType recordType,
      BatchMetaData batchMetaData,
      CustomSourceRecord customSourceRecord) {

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
