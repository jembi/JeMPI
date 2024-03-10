export interface AuditTrail {
  inserted_at: string
  created_at: string
  interaction_id: string
  golden_id: string
  entry: string
  score: number
  linking_rule: string
}

export interface AuditTrailEntries {
  entries: Array<AuditTrail>
}