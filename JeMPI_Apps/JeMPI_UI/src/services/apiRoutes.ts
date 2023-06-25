const ROUTES = {
  GET_FIELDS_CONFIG: '/config',
  GET_LINKED_RECORDS: '/LinkedRecords',
  GET_NOTIFICATIONS: '/MatchesForReview',
  GET_INTERACTION: 'interaction',
  GET_GIDS_PAGED: 'gids-paged',
  GET_GOLDEN_RECORD: '/expanded-golden-record',
  GET_EXPANDED_GOLDEN_RECORDS: '/expanded-golden-records',
  GET_GOLDEN_RECORD_AUDIT_TRAIL: '/golden-record-audit-trail',
  GET_INTERACTION_AUDIT_TRAIL: '/interaction-audit-trail',
  POST_UPDATE_NOTIFICATION: '/NotificationRequest',
  POST_SIMPLE_SEARCH: '/search',
  POST_CUSTOM_SEARCH: '/custom-search',
  PATCH_IID_NEW_GID_LINK: '/Unlink',
  PATCH_IID_GID_LINK: '/Link',
  PATCH_GOLDEN_RECORD: '/golden-record',
  CURRENT_USER: '/current-user',
  VALIDATE_OAUTH: '/authenticate',
  LOGOUT: '/logout',
  AUDIT_TRAIL: '/Audit-trail',
  UPLOAD: '/Upload'
}

export default ROUTES
