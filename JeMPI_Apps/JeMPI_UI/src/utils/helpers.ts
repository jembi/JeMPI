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

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const encodeQueryString = (queryObj: any, nesting = ''): string => {
  const pairs = Object.entries(queryObj).map(([key, val]) => {
    if (Array.isArray(val)) {
      return val
        .map(subVal => [nesting + key, subVal].map(escape).join('='))
        .join('&')
    } else if (typeof val === 'object') {
      return encodeQueryString(val, nesting + `${key}.`)
    } else {
      return [nesting + key, val]
        .map(s => encodeURIComponent(s ? s.toString() : ''))
        .join('=')
    }
  })
  return pairs.join('&')
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const decodeQueryString = <T>(queryString: string): any => {
  const queryStringPieces = queryString.split('&')
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const decodedQueryString: any = {}

  for (const piece of queryStringPieces) {
    // eslint-disable-next-line prefer-const
    let [key, value] = piece.split('=')
    value = value || ''
    if (key in decodedQueryString) {
      const currentValueForKey = decodedQueryString[key]
      if (!Array.isArray(currentValueForKey)) {
        decodedQueryString[key] = [currentValueForKey, value]
      } else {
        currentValueForKey.push(value)
      }
    } else {
      decodedQueryString[key] = value
    }
  }
  return decodedQueryString as T
}
