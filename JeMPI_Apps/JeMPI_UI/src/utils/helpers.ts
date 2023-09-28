import { GridColDef } from '@mui/x-data-grid'
import { AnyRecord } from 'types/PatientRecord'

interface ValidationObject {
  regex?: string
  required: boolean
  onErrorMessage: string
}

export const isInputValid = (value: unknown, validation?: ValidationObject) => {
  if (validation && typeof value === 'string') {
    const regexp = new RegExp(validation.regex || '')
    return !regexp.test(value) || (validation?.required && value.length === 0)
  }
  return false
}

export const sortColumns = (columns: GridColDef[], position: Array<string>) =>
  columns.sort((a, b) => {
    return position.indexOf(a.field) - position.indexOf(b.field)
  })

export const isGoldenRecord = (record: AnyRecord | undefined) =>
  record && 'linkRecords' in record
