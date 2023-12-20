export default interface Notification {
  id: string
  type: NotificationType
  reason?: string | null
  created: Date
  names: string
  patient_id: string | null
  status: NotificationState
  golden_id: string
  score: number
  linkedTo?: GoldenRecordCandidate
  candidates?: GoldenRecordCandidate[]
}

export interface GoldenRecordCandidate {
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

export type NotificationType = 'THRESHOLD' | 'MARGIN' | 'UPDATE'

export type Notifications = {
  records: Notification[]
  pagination: {
    total: number
  }
}
