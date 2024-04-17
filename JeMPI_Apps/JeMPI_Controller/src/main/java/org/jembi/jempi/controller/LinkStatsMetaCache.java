package org.jembi.jempi.controller;

import org.jembi.jempi.shared.models.LinkStatsMeta;

import static org.jembi.jempi.shared.models.CustomFieldTallies.CUSTOM_FIELD_TALLIES_SUM_IDENTITY;

public final class LinkStatsMetaCache {

   private static final LinkStatsMeta LINK_STATS_META_IDENTITY;
   private static LinkStatsMeta linkStatsMeta = null;

   static {
      LINK_STATS_META_IDENTITY =
            new LinkStatsMeta(new LinkStatsMeta.ConfusionMatrix(1.0, 0.0, 0.0, 0.0), CUSTOM_FIELD_TALLIES_SUM_IDENTITY);
   }

   private LinkStatsMetaCache() {
   }

   public static LinkStatsMeta get() {
      final LinkStatsMeta rsp;
      synchronized (LinkStatsMetaCache.class) {
         rsp = linkStatsMeta != null
               ? new LinkStatsMeta(linkStatsMeta.confusionMatrix(), linkStatsMeta.customFieldTallies())
               : LINK_STATS_META_IDENTITY;
      }
      return rsp;
   }

   public static void set(final LinkStatsMeta meta) {
      final var work = new LinkStatsMeta(meta.confusionMatrix(), meta.customFieldTallies());
      synchronized (LinkStatsMetaCache.class) {
         linkStatsMeta = work;
      }
   }

}
