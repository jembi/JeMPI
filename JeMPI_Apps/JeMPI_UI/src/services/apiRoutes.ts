const ROUTES = {
  POST_FIELDS_CONFIG: '/config',
  POST_NOTIFICATIONS: '/notifications',
  POST_INTERACTION: '/interaction',
  POST_DASHBOARD_DATA: '/dashboardData',
  POST_GOLDEN_RECORD: '/expandedGoldenRecord',
  POST_UPDATE_GOLDEN_RECORD_FIELDS: '/updateGoldenRecordFieldsForId',
  POST_EXPANDED_GOLDEN_RECORDS: '/expandedGoldenRecords',
  POST_GOLDEN_RECORD_AUDIT_TRAIL: '/goldenRecordAuditTrail',
  POST_INTERACTION_AUDIT_TRAIL: '/interactionAuditTrail',
  POST_UPDATE_NOTIFICATION: '/notificationRequest',
  POST_SIMPLE_SEARCH: '/search',
  POST_CUSTOM_SEARCH: '/customSearch',
  POST_SIMPLE_GOLDEN_SEARCH: 'simpleGoldenSearch',
  POST_SIMPLE_INTERACTION_PATIENT_SEARCH: 'simpleInteractionSearch',
  POST_CUSTOM_GOLDEN_SEARCH: 'customGoldenSearch',
  POST_CUSTOM_INTERACTION_PATIENT_SEARCH: 'customInteractionSearch',

  POST_CR_CANDIDATES: '/crCandidates',
  POST_IID_NEW_GID_LINK: '/newLink',
  POST_IID_GID_LINK: '/relink',
  CURRENT_USER: '/currentUser',
  VALIDATE_OAUTH: '/authenticate',
  LOGOUT: '/logout',
  UPLOAD: '/upload'
}

export default ROUTES
