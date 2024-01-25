package org.jembi.jempi.controller;

import org.jembi.jempi.shared.models.LinkStatsMeta;

public final class LinkStatsMetaCache {

   private static LinkStatsMeta linkStatsMeta;

   private LinkStatsMetaCache() {
   }

   public static LinkStatsMeta get() {
      final LinkStatsMeta rsp;
      synchronized (LinkStatsMetaCache.class) {
         rsp = new LinkStatsMeta(linkStatsMeta.confusionMatrix(), linkStatsMeta.customFieldTallies());
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
