package org.jembi.jempi.shared.models;

public final class GlobalConstants {

   public static final String TOPIC_INTERACTION_ETL = "JeMPI-interaction-etl";
   public static final String TOPIC_INTERACTION_CONTROLLER = "JeMPI-interaction-controller";
   public static final String TOPIC_INTERACTION_PROCESSOR_CONTROLLER = "JeMPI-interaction-processor-controller";
   public static final String TOPIC_INTERACTION_EM = "JeMPI-interaction-em";
   public static final String TOPIC_INTERACTION_LINKER = "JeMPI-interaction-linker";
   public static final String TOPIC_MU_CONTROLLER = "JeMPI-mu-controller";
   public static final String TOPIC_MU_LINKER = "JeMPI-mu-linker";
   public static final String TOPIC_AUDIT_TRAIL = "JeMPI-audit-trail";
   public static final String TOPIC_NOTIFICATIONS = "JeMPI-notifications";

   public static final String PSQL_TABLE_AUDIT_TRAIL = "audit_trail";

   /*
    *
    * HTTP SEGMENTS
    *
    */
   public static final String SEGMENT_COUNT_INTERACTIONS = "countInteractions";
   public static final String SEGMENT_COUNT_GOLDEN_RECORDS = "countGoldenRecords";
   public static final String SEGMENT_COUNT_RECORDS = "countRecords";
   public static final String SEGMENT_POST_GIDS_ALL = "gidsAll";
   public static final String SEGMENT_POST_GIDS_PAGED = "gidsPaged";
   public static final String SEGMENT_POST_INTERACTION = "interaction";
   public static final String SEGMENT_POST_EXPANDED_GOLDEN_RECORD = "expandedGoldenRecord";
   public static final String SEGMENT_POST_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST = "expandedGoldenRecords";
   public static final String SEGMENT_POST_EXPANDED_GOLDEN_RECORDS_USING_CSV = "expandedGoldenRecordsCsv";
   public static final String SEGMENT_POST_EXPANDED_INTERACTIONS_USING_CSV = "expandedInteractionsCsv";
   public static final String SEGMENT_POST_GOLDEN_RECORD_AUDIT_TRAIL = "goldenRecordAuditTrail";
   public static final String SEGMENT_POST_INTERACTION_AUDIT_TRAIL = "interactionAuditTrail";
   public static final String SEGMENT_POST_FIELDS_CONFIG = "config";
   public static final String SEGMENT_POST_LINKED_RECORDS = "linkedRecords";
   public static final String SEGMENT_POST_NOTIFICATIONS = "matchesForReview";
   public static final String SEGMENT_PATCH_GOLDEN_RECORD = "goldenRecord";
   public static final String SEGMENT_POST_IID_GID_LINK = "link";
   public static final String SEGMENT_POST_IID_NEW_GID_LINK = "unLink";
   public static final String SEGMENT_POST_UPDATE_NOTIFICATION = "notificationRequest";
   public static final String SEGMENT_POST_SIMPLE_SEARCH = "search";
   public static final String SEGMENT_POST_CUSTOM_SEARCH = "customSearch";
   public static final String SEGMENT_POST_UPLOAD_CSV_FILE = "upload";
   public static final String SEGMENT_POST_FILTER_GIDS = "filterGids";
   public static final String SEGMENT_POST_FILTER_GIDS_WITH_INTERACTION_COUNT = "filterGidsInteraction";
   public static final String SEGMENT_PROXY_POST_CR_REGISTER = "crRegister";
   public static final String SEGMENT_PROXY_POST_CR_LINK_TO_GID_UPDATE = "crLinkToGidUpdate";
   public static final String SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID = "crLinkBySourceId";
   public static final String SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID_UPDATE = "crLinkBySourceIdUpdate";
   public static final String SEGMENT_PROXY_POST_CR_FIND = "crFind";
   public static final String SEGMENT_PROXY_POST_CR_CANDIDATES = "crCandidates";
   public static final String SEGMENT_PROXY_PATCH_CR_UPDATE_FIELDS = "crUpdateFields";
   public static final String SEGMENT_POST_CR_FIND_SOURCE_ID = "crFindSourceId";
   public static final String SEGMENT_PROXY_POST_CANDIDATES_WITH_SCORES = "candidateGoldenRecords";
   public static final String SEGMENT_PROXY_POST_CALCULATE_SCORES = "calculateScores";
   public static final String SEGMENT_PROXY_POST_DASHBOARD_DATA = "dashboardData";
   public static final String SEGMENT_PROXY_ON_NOTIFICATION_RESOLUTION = "onNotificationResolution";
   public static final String SEGMENT_PROXY_POST_LINK_INTERACTION = "linkInteraction";
   // public static final String SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID =
   // "linkInteractionToGid";
   public static final String SEGMENT_VALIDATE_OAUTH = "authenticate";
   public static final String SEGMENT_LOGOUT = "logout";
   public static final String SEGMENT_CURRENT_USER = "currentUser";

   // TIMEOUTS
   public static final int TIMEOUT_DGRAPH_RECONNECT_RETRIES = 20;
   public static final int TIMEOUT_DGRAPH_RECONNECT_SLEEP_SECS = 2;
   public static final int TIMEOUT_DGRAPH_QUERY_SECS = (TIMEOUT_DGRAPH_RECONNECT_SLEEP_SECS
         * TIMEOUT_DGRAPH_RECONNECT_RETRIES);
   public static final int TIMEOUT_GENERAL_SECS = 60;
   public static final int TIMEOUT_TEA_TIME_SECS = 5;

   private GlobalConstants() {
   }

   public enum AuditEventType {
      LINKING_EVENT,
      NOTIFICATION_EVENT,
      UNKNOWN_EVENT
   }

}
