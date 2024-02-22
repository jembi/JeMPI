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

export const MatchingRule = {
  DETERMINISTIC: 'DETERMINISTIC',
  PROBABLISTIC: 'PROBABLISTIC',
  UNKNOWN: ''
} as const;

