import moment from 'moment'
import { FieldType } from '../types/Fields'
import { AnyRecord, ValueOf } from '../types/PatientRecord'

export const formatDate = (value: Date) => {
  return moment(value).format('DD/MM/YYYY')
}

export const getFieldValueFormatter = (type: FieldType) => {
  return (value: ValueOf<AnyRecord>): string | undefined => {
    switch (type) {
      case 'Date':
        return value ? formatDate(value as Date) : undefined
      default:
        return value?.toString()
    }
  }
}

export const formatName = (value: string) => {
  const fullName = value.split(',')
  return `${fullName[0] ? fullName[0] : ''} ${fullName[1] ? fullName[1] : ''}`
}
