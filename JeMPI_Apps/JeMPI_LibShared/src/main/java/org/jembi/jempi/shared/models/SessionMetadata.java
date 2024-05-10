package org.jembi.jempi.shared.models;

public class SessionMetadata {
   public CommonMetaData commonMetaData;
   public UIMetadata uiMetadata;
   public AsyncReceiverMetadata asyncReceiverMetadata;
   public ETLMetadata etlMetadata;
   public ControllerMetadata controllerMetadata;
   public LinkerMetadata linkerMetadata;

   public SessionMetadata() { }

   public SessionMetadata(
         final CommonMetaData commonMetaData,
         final UIMetadata uiMetadata,
         final AsyncReceiverMetadata asyncReceiverMetadata,
         final ETLMetadata etlMetadata,
         final ControllerMetadata controllerMetadata,
         final LinkerMetadata linkerMetadata) {
      this.commonMetaData = commonMetaData;
      this.uiMetadata = uiMetadata;
      this.asyncReceiverMetadata = asyncReceiverMetadata;
      this.etlMetadata = etlMetadata;
      this.controllerMetadata = controllerMetadata;
      this.linkerMetadata = linkerMetadata;
   }
}
