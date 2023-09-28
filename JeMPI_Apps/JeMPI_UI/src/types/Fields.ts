import { GridValueGetterParams, GridTreeNodeWithRender } from '@mui/x-data-grid'
import { AnyRecord, DemographicData, SourceId, ValueOf } from './PatientRecord'

export type FieldGroup =
  | 'none'
  | 'name'
  | 'identifiers'
  | 'registering_facility'
  | 'address'
  | 'demographics'
  | 'relationships'
  | 'sub_heading'
  | 'system'
  | 'linked_records'
  | 'audit_trail'
  | 'filter'

export type FieldType = 'String' | 'Number' | 'Date' | 'Boolean' | 'SourceId'

export interface Field {
  fieldName: string
  fieldType: FieldType
  fieldLabel: string
  groups: FieldGroup[]
  scope: string[]
  accessLevel: string[]
  readOnly?: boolean
  validation?: { required: boolean; regex?: string; onErrorMessage: string }
}

export interface FieldChangeReq {
  fields: Array<{
    name: string
    oldValue: FieldType
    newValue: FieldType
  }>
}

export interface DisplayField extends Field {
  formatValue: (v: ValueOf<AnyRecord>) => string | number | undefined
  isValid: (value: ValueOf<AnyRecord>) => boolean
  getValue: (
    params: GridValueGetterParams<AnyRecord, any, GridTreeNodeWithRender>
  ) => string | number | SourceId | SourceId[] | DemographicData | undefined
}

export type Fields = Field[]
