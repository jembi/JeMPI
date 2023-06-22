import dayjs from 'dayjs'
import { FieldType } from '../types/Fields'
import { AnyRecord, ValueOf } from '../types/PatientRecord'

export const formatDate = (value: Date) => {
  return dayjs(value).format('YYYY/MM/DD')
}

export const formatNumber = (value: number) => value.toFixed(3)

export const getFieldValueFormatter = (type: FieldType) => {
  return (value: ValueOf<AnyRecord>): string | undefined => {
    switch (type) {
      case 'Number':
        return value ? formatNumber(value as number) : undefined
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
