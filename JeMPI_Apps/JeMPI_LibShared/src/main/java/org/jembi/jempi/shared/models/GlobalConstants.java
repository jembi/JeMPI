package org.jembi.jempi.shared.models;

public final class GlobalConstants {

   public static final String TOPIC_INTERACTION_ASYNC_ETL = "JeMPI-async-etl";
   public static final String TOPIC_INTERACTION_CONTROLLER = "JeMPI-interaction-controller";
   public static final String TOPIC_INTERACTION_EM = "JeMPI-interaction-em";
   public static final String TOPIC_INTERACTION_LINKER = "JeMPI-interaction-linker";
   public static final String TOPIC_MU_LINKER = "JeMPI-mu-linker";
   public static final String TOPIC_AUDIT_TRAIL = "JeMPI-audit-trail";
   public static final String TOPIC_NOTIFICATIONS = "JeMPI-notifications";

   public static final String PSQL_TABLE_AUDIT_TRAIL = "AuditTrail";

   public static final String SEGMENT_GET_LINKED_RECORDS = "LinkedRecords";
   //                         SEGMENT_GET_LINKED_RECORDS: '/LinkedRecords',
   public static final String SEGMENT_GET_NOTIFICATIONS = "MatchesForReview";
   //                         SEGMENT_GET_NOTIFICATIONS: '/MatchesForReview',
   public static final String SEGMENT_PATIENT_RECORD_ROUTE = "patient-record";
   //                         SEGMENT_PATIENT_RECORD_ROUTE: '/patient-record',
   public static final String SEGMENT_GOLDEN_RECORD_ROUTE = "golden-record";
   //                         SEGMENT_GOLDEN_RECORD_ROUTE: '/golden-record',
   public static final String SEGMENT_GET_GOLDEN_ID_DOCUMENTS = "GoldenRecord";
   //                         SEGMENT_GET_GOLDEN_ID_DOCUMENTS: '/GoldenRecord',
   public static final String SEGMENT_UPDATE_NOTIFICATION = "NotificationRequest";
   //                         SEGMENT_UPDATE_NOTIFICATION: '/NotificationRequest',
   public static final String SEGMENT_CREATE_GOLDEN_RECORD = "Unlink";
   //                         SEGMENT_CREATE_GOLDEN_RECORD: '/Unlink',
   public static final String SEGMENT_LINK_RECORD = "Link";
   //                         SEGMENT_LINK_RECORD: '/Link',
   public static final String SEGMENT_POST_SIMPLE_SEARCH = "search";
   //                         SEGMENT_POST_SIMPLE_SEARCH: '/search',
   public static final String SEGMENT_POST_CUSTOM_SEARCH = "custom-search";
   //                         SEGMENT_POST_CUSTOM_SEARCH: '/custom-search',
   //                         SEGMENT_CURRENT_USER: '/current-user',
   //                         SEGMENT_VALIDATE_OAUTH: '/authenticate',
   //                         SEGMENT_LOGOUT: '/logout',
   //                         SEGMENT_AUDIT_TRAIL: '/Audit-trail',
   public static final String SEGMENT_UPLOAD = "Upload";
   //                         SEGMENT_UPLOAD: '/Upload',
   public static final String SEGMENT_UPDATE_GOLDEN_RECORD = "golden-record";
   //                         SEGMENT_UPDATE_GOLDEN_RECORD: '/golden-record',
   public static final String SEGMENT_EXPANDED_GOLDEN_RECORDS = "expanded-golden-records";
   //                         SEGMENT_EXPANDED_GOLDEN_RECORDS: '/expanded-golden-records'
   public static final String SEGMENT_GET_FIELDS_CONFIG = "config";
   public static final String SEGMENT_CALCULATE_SCORES = "calculate-scores";
   public static final String SEGMENT_COUNT_GOLDEN_RECORDS = "count-golden-records";
   public static final String SEGMENT_COUNT_PATIENT_RECORDS = "count-patient-records";
   public static final String SEGMENT_COUNT_RECORDS = "count-records";
   public static final String SEGMENT_GOLDEN_IDS = "golden-ids";
   public static final String SEGMENT_EXPANDED_PATIENT_RECORDS = "expanded-patient-records";
   public static final String SEGMENT_CANDIDATE_GOLDEN_RECORDS = "candidate-golden-records";
   public static final String SEGMENT_VALIDATE_OAUTH = "authenticate";
   public static final String SEGMENT_LOGOUT = "logout";
   public static final String SEGMENT_CURRENT_USER = "current-user";


   private GlobalConstants() {
   }

}
