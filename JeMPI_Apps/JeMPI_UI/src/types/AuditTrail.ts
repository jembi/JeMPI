export interface AuditTrail {
  inserted_at: string
  created_at: string
  interaction_id: string
  golden_id: string
  entry: string
  score: number
}

export interface AuditTrailEntries {
  entries: Array<AuditTrail>
}
