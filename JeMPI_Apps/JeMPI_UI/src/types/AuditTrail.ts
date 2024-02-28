export interface AuditTrail {
  inserted_at: string
  created_at: string
  interaction_id: string
  golden_id: string
  entry: string
  linking_rule: LinkingRule
}

export interface AuditTrailEntries {
  entries: Array<AuditTrail>
}

export interface LinkingRule {
    text: string
    matchType: string
}
