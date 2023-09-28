export type DemographicData = Record<string, string | number>

export interface SourceId {
  uid: string
  facility: string
  patient: string
}

export interface BaseRecord {
  uid: string
  demographicData: DemographicData
  createdAt: string
  auxId: string
}

export interface PatientRecord extends BaseRecord {
  score?: number
  sourceId: SourceId
}

export interface GoldenRecord extends BaseRecord {
  sourceId: SourceId[]
  linkRecords: PatientRecord[]
  type?: 'Current' | 'Blocked' | 'Searched'
}

export type ValueOf<T> = T[keyof T]

export type AnyRecord = GoldenRecord | PatientRecord
