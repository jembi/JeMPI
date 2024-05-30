import { GridColDef } from '@mui/x-data-grid'
import { Configuration, CustomNode, Field, LinkingRules } from 'types/Configuration'
import { AnyRecord } from 'types/PatientRecord'

interface ValidationObject {
  regex?: string
  required: boolean
  onErrorMessage: string
}

type Params = {
  row?: {
    fieldName?: string
  }
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

export const randomId = () => {
  return Math.random().toString(36).substring(2, 9)
}



export const generateId = (configuration: Configuration): Configuration => {
  const generateIdForFields = (fields: Field[]): Field[] => {
    return fields.map((item) => ({ ...item, id: Math.random().toString(36).substr(2, 9) }));
  };

  const generateIdForNodes = (nodes: CustomNode[]): CustomNode[] => {
    return nodes.map((node) => ({
      ...node,
      id: Math.random().toString(36).substr(2, 9),
      fields: generateIdForFields(node.fields),
    }));
  };

  return {
    ...configuration,
    auxInteractionFields: generateIdForFields(configuration.auxInteractionFields),
    auxGoldenRecordFields: generateIdForFields(configuration.auxGoldenRecordFields),
    demographicFields: generateIdForFields(configuration.demographicFields),
    additionalNodes: generateIdForNodes(configuration.additionalNodes),
  };
};
  
export function processIndex(index: string) {
  if (index) {
    return index.replace(/@index\(|\)(?=, trigram|$)/g, ' ').replace(/,/g, ', ')
  }
  return ''
}

export const transformFieldName = (input: Params | string): string => {
  const fieldName =
    typeof input === 'string' ? input : input?.row?.fieldName || 'Unknown Field'
  return fieldName
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (char: string) => char.toUpperCase())
}

export const toSnakeCase = (str: string) =>
  str
    .replace(/([A-Z])/g, letter => `_${letter.toLowerCase()}`)
    .replace(/^_/, '')

export const formatNodeName = (nodeName: string): string => {
  return nodeName
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(
      /\b(\w)(\w*)/g,
      (_, first, rest) => first.toUpperCase() + rest.toLowerCase()
    )
    .replace(/\bId\b/g, 'ID')
    .trim()
}

export const toUpperCase = (word: string): string => {
  return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
}
