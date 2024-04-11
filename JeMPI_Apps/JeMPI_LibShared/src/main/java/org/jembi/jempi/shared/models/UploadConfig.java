package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadConfig(
        Boolean reportingRequired,
        Integer uploadWorkflow,
        Double minThreshold,
        Double linkThreshold,
        Double maxThreshold,
        Double marginWindowSize) {

   public enum UploadWorkflow {
      WORKFLOW_LINK(UploadWorkflow.UPLOAD_WORKFLOW_LINK),
      WORKFLOW_EM(UploadWorkflow.UPLOAD_WORKFLOW_EM),
      WORKFLOW_EM_LINK(UploadWorkflow.UPLOAD_WORKFLOW_EM_LINK);
      public static final int UPLOAD_WORKFLOW_LINK = 0;
      public static final int UPLOAD_WORKFLOW_EM = 1;
      public static final int UPLOAD_WORKFLOW_EM_LINK = 2;

      public final int type;

      UploadWorkflow(final int type_) {
         this.type = type_;
      }
   }
}
