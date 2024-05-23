package org.jembi.jempi.shared.models;

public record SessionMetadata(
      CommonMetaData commonMetaData,
      UIMetadata uiMetadata,
      AsyncReceiverMetadata asyncReceiverMetadata,
      ETLMetadata etlMetadata,
      ControllerMetadata controllerMetadata,
      LinkerMetadata linkerMetadata
) {
}
