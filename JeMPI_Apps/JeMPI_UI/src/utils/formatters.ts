import dayjs from 'dayjs'
import { FieldType } from '../types/Fields'
import { AnyRecord, ValueOf } from '../types/PatientRecord'
import { GridValueGetterParams } from '@mui/x-data-grid'

export const formatDate = (value: Date) => dayjs(value).format('YYYY/MM/DD')

export const formatDateTime = (value: Date) =>
  dayjs(value).format('YYYY/MM/DD HH:MM:ss ')

export const formatNumber = (value: number) => value.toFixed(3)

export const getFieldValueFormatter = (type: FieldType) => {
  return (value: ValueOf<AnyRecord>): string | undefined => {
    switch (type) {
      case 'Number':
        return value ? formatNumber(value as unknown as number) : undefined
      case 'Date':
        return value ? formatDate(value as unknown as Date) : undefined
      default:
        return value?.toString()
    }
  }
}

export const valueGetter = (params: GridValueGetterParams<AnyRecord, any>) => {
  const { row, field } = params
  const f = field as keyof AnyRecord
  return field in row.demographicData
    ? row.demographicData[f]
    : f in row
    ? row[f]
    : undefined
}

export const formatName = (value: string) => {
  const fullName = value.split(',')
  return `${fullName[0] ? fullName[0] : ''} ${fullName[1] ? fullName[1] : ''}`
}

export const truncateString = (value: string, maxLength: number) => {
  if (value.length > maxLength) return `${value.substring(0, maxLength)}...`
  return value
}

export const formatBytesSize = (maxSizeInBytes: number | string) => {
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = Number(maxSizeInBytes)
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  const convertedSize = size.toFixed(2)
  const unit = units[unitIndex]
  return `${convertedSize} ${unit}`
}
