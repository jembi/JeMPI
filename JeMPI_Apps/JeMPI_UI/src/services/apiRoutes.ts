const ROUTES = {
  GET_FIELDS_CONFIG: '/config',
  GET_LINKED_RECORDS: '/linkedRecords',
  GET_NOTIFICATIONS: '/matchesForReview',
  POST_INTERACTION: '/interaction',
  GET_DASHBOARD_DATA: '/dashboardData',
  GET_GIDS_PAGED: '/gidsPaged',
  GET_GOLDEN_RECORD: '/expandedGoldenRecord',
  GET_EXPANDED_GOLDEN_RECORDS: '/expandedGoldenRecords',
  GET_GOLDEN_RECORD_AUDIT_TRAIL: '/goldenRecordAuditTrail',
  POST_INTERACTION_AUDIT_TRAIL: '/interactionAuditTrail',
  POST_UPDATE_NOTIFICATION: '/notificationRequest',
  POST_SIMPLE_SEARCH: '/search',
  POST_CUSTOM_SEARCH: '/customSearch',
  POST_FILTER_GIDS: '/filterGids',
  POST_FILTER_GIDS_WITH_INTERACTION_COUNT: '/filterGidsInteraction',
  POST_CR_CANDIDATES: '/crCandidates',
  POST_IID_NEW_GID_LINK: '/unLink',
  POST_IID_GID_LINK: '/link',
  PATCH_GOLDEN_RECORD: '/goldenRecord',
  CURRENT_USER: '/currentUser',
  VALIDATE_OAUTH: '/authenticate',
  LOGOUT: '/logout',
  AUDIT_TRAIL: '/auditTrail',
  UPLOAD: '/upload'
}

export default ROUTES
