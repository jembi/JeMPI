package org.jembi.jempi.shared.models;

import java.util.HashMap;
import java.util.List;

public final class MatchesForReviewResult {
   List<HashMap<String, Object>> notifications;
   int count;
   int skippedRecords;

   public List<HashMap<String, Object>> getNotifications() {
      return notifications;
   }

   public void setNotifications(final List<HashMap<String, Object>> notifications) {
      this.notifications = notifications;
   }

   public int getCount() {
      return count;
   }

   public void setCount(final int count) {
      this.count = count;
   }

   public int getSkippedRecords() {
      return skippedRecords;
   }

   public void setSkippedRecords(final int skippedRecords) {
      this.skippedRecords = skippedRecords;
   }
}
