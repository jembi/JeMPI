package org.jembi.jempi.controller;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.jembi.jempi.shared.models.CustomFieldTallies;

public class SPTalliesMU extends ContextualProcessor<String, CustomFieldTallies, Void, Void> {

   public static final String STATE_STORE_NAME = "tallies-abcd";

   @Override
   public void process(final Record<String, CustomFieldTallies> recordToProcess) {
      final KeyValueStore<String, CustomFieldTallies> keyValueStore = super.context().getStateStore(STATE_STORE_NAME);
      final var tallies = keyValueStore.get("Total");
      if (tallies == null) {
         keyValueStore.put("Total", recordToProcess.value());
      } else {
         final var updated = tallies.sum(recordToProcess.value());
         updated.logFieldMU();
         keyValueStore.put("Total", updated);
      }

   }

}

