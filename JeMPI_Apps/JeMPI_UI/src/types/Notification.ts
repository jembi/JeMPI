export default interface Notification {
  id: string
  type: string
  reason?: string | null
  created: Date
  names: string
  patient_id: string | null
  status: NotificationState
  golden_id: string
  score: number
  linkedTo?: GoldenRecord
  candidates?: GoldenRecord[]
}

export interface GoldenRecord {
  golden_id: string
  score: number
}

export enum NotificationState {
  New = 'New',
  Seen = 'Seen',
  Actioned = 'Actioned',
  Pending = 'Pending',
  Accepted = 'Accepted'
}
