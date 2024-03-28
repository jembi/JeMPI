export default interface Notification {
  id: string
  type: NotificationType
  reason?: string | null
  created: Date
  names: string
  patient_id: string | null
  status: NotificationState
  old_golden_id: string
  current_golden_id: string
  score: number
  linkedTo?: GoldenRecordCandidate
  candidates?: GoldenRecordCandidate[]
}

export interface GoldenRecordCandidate {
  golden_id: string
  score: number
}

export enum NotificationState {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  ALL = 'ALL'
}

export type NotificationType = 'THRESHOLD' | 'MARGIN' | 'UPDATE'

export type Notifications = {
  records: Notification[]
  pagination: {
    total: number
  }
}
