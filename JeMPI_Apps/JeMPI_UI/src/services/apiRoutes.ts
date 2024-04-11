const ROUTES = {
  POST_FIELDS_CONFIG: '/config',
  POST_LINKED_RECORDS: '/linkedRecords',
  POST_NOTIFICATIONS: '/matchesForReview',
  POST_INTERACTION: '/interaction',
  POST_DASHBOARD_DATA: '/dashboardData',
  POST_GIDS_PAGED: '/gidsPaged',
  POST_GOLDEN_RECORD: '/expandedGoldenRecord',
  POST_EXPANDED_GOLDEN_RECORDS: '/expandedGoldenRecords',
  POST_GOLDEN_RECORD_AUDIT_TRAIL: '/goldenRecordAuditTrail',
  POST_INTERACTION_AUDIT_TRAIL: '/interactionAuditTrail',
  POST_UPDATE_NOTIFICATION: '/notificationRequest',
  POST_SIMPLE_SEARCH: '/search',
  POST_CUSTOM_SEARCH: '/customSearch',
  POST_FILTER_GIDS: '/filterGids',
  POST_FILTER_GIDS_WITH_INTERACTION_COUNT: '/filterGidsInteraction',
  POST_CR_CANDIDATES: '/crCandidates',
  POST_IID_NEW_GID_LINK: '/newLink',
  POST_IID_GID_LINK: '/relink',
  PATCH_GOLDEN_RECORD: '/goldenRecord',
  CURRENT_USER: '/currentUser',
  VALIDATE_OAUTH: '/authenticate',
  LOGOUT: '/logout',
  AUDIT_TRAIL: '/auditTrail',
  UPLOAD: '/upload'
}

export default ROUTES
