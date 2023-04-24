import { AnyRecord, ValueOf } from './PatientRecord'

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

export type FieldType = 'String' | 'Number' | 'Date' | 'Boolean' | 'SourceId'

export interface Field {
  fieldName: string
  fieldType: FieldType
  fieldLabel: string
  groups: FieldGroup[]
  scope: string[]
  accessLevel: string[]
  readOnly?: boolean
  rules?: { required: boolean; regex: string }
}

export interface FieldChangeReq {
  fields: {
    name: string
    value: FieldType
  }[]
}

export interface DisplayField extends Field {
  formatValue: (v: ValueOf<AnyRecord>) => string | undefined
  isValid: (value: ValueOf<AnyRecord>) => boolean
}

export type Fields = Field[]
