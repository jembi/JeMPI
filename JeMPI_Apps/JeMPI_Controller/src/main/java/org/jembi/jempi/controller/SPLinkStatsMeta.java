package org.jembi.jempi.controller;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.jembi.jempi.shared.models.LinkStatsMeta;

public class SPLinkStatsMeta extends ContextualProcessor<String, LinkStatsMeta, Void, Void> {

   public static final String STATE_STORE_NAME = "link-stats-meta";

   @Override
   public void process(final Record<String, LinkStatsMeta> recordToProcess) {

      final KeyValueStore<String, LinkStatsMeta> keyValueStore = super.context().getStateStore(STATE_STORE_NAME);
      final var linkStatsMeta = keyValueStore.get("Totals");

      if (linkStatsMeta == null) {
         keyValueStore.put("Totals", recordToProcess.value());
      } else {
         final var updatedConfusionMatrix =
               linkStatsMeta.confusionMatrix().sum(recordToProcess.value().confusionMatrix());
         final var updatedCustomFieldTallies =
               linkStatsMeta.fieldTallies().sum(recordToProcess.value().fieldTallies());
         final var updatedLinkStatsMeta = new LinkStatsMeta(updatedConfusionMatrix, updatedCustomFieldTallies);
         LinkStatsMetaCache.set(updatedLinkStatsMeta);
         keyValueStore.put("Totals", updatedLinkStatsMeta);
      }

   }

}

