export interface AuditTrail {
  inserted_at: string
  created_at: string
  interaction_id: string
  golden_id: string
  entry: string
}

export interface AuditTrailEntries {
  entries: Array<AuditTrail>
}

export interface ExpandedAuditTrail extends AuditTrail {
    matching_rule: string | null
}
