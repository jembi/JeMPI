package org.jembi.jempi.shared.models;

public final class GlobalConstants {

   public static final String TOPIC_INTERACTION_ASYNC_ETL = "JeMPI-async-etl";
   public static final String TOPIC_INTERACTION_CONTROLLER = "JeMPI-interaction-controller";
   public static final String TOPIC_INTERACTION_EM = "JeMPI-interaction-em";
   public static final String TOPIC_INTERACTION_LINKER = "JeMPI-interaction-linker";
   public static final String TOPIC_MU_LINKER = "JeMPI-mu-linker";
   public static final String TOPIC_AUDIT_TRAIL = "JeMPI-audit-trail";
   public static final String TOPIC_NOTIFICATIONS = "JeMPI-notifications";


   public static final String PSQL_TABLE_AUDIT_TRAIL = "audit_trail";


   /*
    *
    * HTTP SEGMENTS
    *
    */
   public static final String SEGMENT_COUNT_INTERACTIONS = "count-interactions";
   public static final String SEGMENT_COUNT_GOLDEN_RECORDS = "count-golden-records";
   public static final String SEGMENT_COUNT_RECORDS = "count-records";

   public static final String SEGMENT_GET_GIDS_ALL = "gids-all";
   public static final String SEGMENT_GET_GIDS_PAGED = "gids-paged";
   public static final String SEGMENT_GET_INTERACTION = "interaction";
   public static final String SEGMENT_GET_EXPANDED_GOLDEN_RECORD = "expanded-golden-record";
   public static final String SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST = "expanded-golden-records";
   public static final String SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_CSV = "expanded-golden-records-csv";
   public static final String SEGMENT_GET_EXPANDED_INTERACTIONS_USING_CSV = "expanded-interactions-csv";
   public static final String SEGMENT_GET_GOLDEN_RECORD_AUDIT_TRAIL = "golden-record-audit-trail";
   public static final String SEGMENT_GET_INTERACTION_AUDIT_TRAIL = "interaction-audit-trail";
   public static final String SEGMENT_GET_FIELDS_CONFIG = "config";
   public static final String SEGMENT_GET_LINKED_RECORDS = "LinkedRecords";
   public static final String SEGMENT_GET_NOTIFICATIONS = "MatchesForReview";

   public static final String SEGMENT_PATCH_GOLDEN_RECORD = "golden-record";
   public static final String SEGMENT_PATCH_IID_GID_LINK = "Link";
   public static final String SEGMENT_PATCH_IID_NEW_GID_LINK = "Unlink";

   public static final String SEGMENT_POST_UPDATE_NOTIFICATION = "NotificationRequest";
   public static final String SEGMENT_POST_SIMPLE_SEARCH = "search";
   public static final String SEGMENT_POST_CUSTOM_SEARCH = "custom-search";
   public static final String SEGMENT_POST_UPLOAD_CSV_FILE = "Upload";
   public static final String SEGMENT_POST_FILTER_GIDS = "filter-gids";
   public static final String SEGMENT_POST_FILTER_GIDS_WITH_INTERACTION_COUNT = "filter-gids-interaction";

   public static final String SEGMENT_PROXY_CR_REGISTER = "cr-register";
   public static final String SEGMENT_PROXY_CR_FIND = "cr-find";
   public static final String SEGMENT_PROXY_CR_CANDIDATES = "cr-candidates";
   public static final String SEGMENT_PROXY_CR_UPDATE_FIELDS = "cr-update-fields";


   public static final String SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES = "candidate-golden-records";
   public static final String SEGMENT_PROXY_POST_CALCULATE_SCORES = "calculate-scores";

   public static final String SEGMENT_PROXY_UPDATE_M_AND_U_ON_NOTI_RESOLUTION = "m-and-u-noti-resolution";
   public static final String SEGMENT_PROXY_POST_LINK_INTERACTION = "link-interaction";
   public static final String SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID = "link-interaction-to-gid";


   public static final String SEGMENT_VALIDATE_OAUTH = "authenticate";
   //                         SEGMENT_VALIDATE_OAUTH: '/authenticate',
   public static final String SEGMENT_LOGOUT = "logout";
   //                         SEGMENT_LOGOUT: '/logout',
   public static final String SEGMENT_CURRENT_USER = "current-user";
   //                         SEGMENT_CURRENT_USER: '/current-user',


   public static final String DEFAULT_LINKER_M_AND_U_GLOBAL_STORE_NAME = "linker";
   private GlobalConstants() {
   }

}
