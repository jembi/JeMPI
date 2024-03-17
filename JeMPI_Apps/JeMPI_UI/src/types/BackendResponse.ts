import Notification, { NotificationState } from './Notification'
import {
  PatientRecord as PR,
  AnyRecord,
  DemographicData,
  SourceId
} from '../types/PatientRecord'

export interface NotificationRequest {
  notificationId: string
  oldGoldenId: string
  currentGoldenId: string
}

export interface LinkRequest {
  notificationId: string
  notificationType: string
  interactionId: string
  resolutionState: string
  currentGoldenId: string
  currentCandidates: string[]
  newGoldenId: string
  score?:number
}

export interface NotificationResponse {
  records: Notification[]
  skippedRecords: number
  count: number
}

export interface GoldenRecordResponse {
  expandedGoldenRecords: ExpandedGoldenRecordResponse[]
}

export interface UniqueGoldenRecordData {
  auxDateCreated: string
  auxAutoUpdateEnabled: boolean
  auxId: string
}

export interface GoldenRecordResponse {
  uid: string
  sourceId: SourceId[]
  demographicData: DemographicData
  uniqueGoldenRecordData: UniqueGoldenRecordData
}

export interface ExpandedGoldenRecordResponse {
  goldenRecord: GoldenRecordResponse
  interactionsWithScore: Array<InteractionWithScore>
}

export interface InteractionWithScore {
  interaction: Interaction
  score: number
}

export interface UniqueInteractionData {
  auxDateCreated: string
  auxId: string
  auxClinicalData: string
}

export interface Interaction {
  uid: string
  sourceId: SourceId
  demographicData: DemographicData
  uniqueInteractionData: UniqueInteractionData
}

export interface DashboardData {
  // TODO: Later make the types more specifc
  sqlDashboardData: any
  dashboardData: any
}

export interface GoldenRecordCandidatesResponse {
  goldenRecords: {
    goldenId: string
    sourceId: SourceId[]
    customUniqueGoldenRecordData: UniqueGoldenRecordData
    demographicData: DemographicData
  }[]
}
