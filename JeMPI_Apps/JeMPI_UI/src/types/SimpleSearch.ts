import { AnyRecord, SourceId } from './PatientRecord'

export interface SearchParameter {
  fieldName: string
  value: string | Date
  distance: number
}

export interface SimpleSearchQuery {
  parameters: SearchParameter[]
}

export interface BaseSearchQuery {
  sortBy: string
  sortAsc: boolean
  offset: number
  limit: number
}

export interface CustomSearchQuery extends BaseSearchQuery {
  $or: SimpleSearchQuery[]
}
export interface SearchQuery extends BaseSearchQuery {
  parameters: SearchParameter[]
}

export interface FilterQuery extends BaseSearchQuery {
  parameters: SearchParameter[]
  createdAt: string
}

export enum FlagLabel {
  ALL_RECORDS = 'ALL RECORDS',
  GOLDEN_ONLY = 'GOLDEN ONLY',
  PATIENT_ONLY = 'PATIENT ONLY'
}

export interface ToggleButtonOptions {
  value: number
  label: string
}

export interface JempiGoldenRecord {
  goldenRecord: {
    demographicData: Omit<AnyRecord, 'uid' | 'sourceId'>
    sourceId?: SourceId[]
    uid: unknown
  }
  mpiPatientRecords: Array<{
    patientRecord: {
      demographicData: Omit<AnyRecord, 'uid' | 'sourceId'>
      sourceId?: SourceId[]
      uid: unknown
    }
  }>
}

export interface JempiPatientRecord {
  demographicData: Omit<AnyRecord, 'uid' | 'sourceId'>
  sourceId?: SourceId[]
  uid: unknown
}

export type ApiSearchResult = {
  records: {
    data: Array<JempiGoldenRecord | JempiPatientRecord>
    pagination: {
      total: number
    }
  }
}
