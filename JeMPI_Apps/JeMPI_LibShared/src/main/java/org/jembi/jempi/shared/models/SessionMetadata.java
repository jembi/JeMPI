package org.jembi.jempi.shared.models;

public class SessionMetadata {
   public UIMetadata uiMetadata;
   public AsyncReceiverMetadata asyncReceiverMetadata;
   public ETLMetadata etlMetadata;
   public ControllerMetadata controllerMetadata;
   public LinkerMetadata linkerMetadata;

   public SessionMetadata() { }

   public SessionMetadata(
         final UIMetadata uiMetadata,
         final AsyncReceiverMetadata asyncReceiverMetadata,
         final ETLMetadata etlMetadata,
         final ControllerMetadata controllerMetadata,
         final LinkerMetadata linkerMetadata) {
      this.uiMetadata = uiMetadata;
      this.asyncReceiverMetadata = asyncReceiverMetadata;
      this.etlMetadata = etlMetadata;
      this.controllerMetadata = controllerMetadata;
      this.linkerMetadata = linkerMetadata;
   }
}
