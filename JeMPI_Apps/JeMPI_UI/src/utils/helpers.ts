import { GridColDef } from '@mui/x-data-grid'

export const isInputValid = (
  value: unknown,
  rules?: { regex: string; required: boolean }
) => {
  if (rules && typeof value === 'string') {
    const regexp = new RegExp(rules.regex || '')
    return !regexp.test(value) || (rules?.required && value.length === 0)
  }
  return false
}

export const sortColumns = (columns: GridColDef[], position: Array<string>) =>
  columns.sort((a, b) => {
    return position.indexOf(a.field) - position.indexOf(b.field)
  })
