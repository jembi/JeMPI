import { Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material'
import { GridColDef } from '@mui/x-data-grid'
import React, { useState } from 'react'
import SelectMatchLevelMenu from './SelectMatchLevelMenu'
import TableCellInput from './TableCellInput'
import { useAppConfig } from 'hooks/useAppConfig'
import { SearchParameter } from 'types/SimpleSearch'

interface SearchTableFormProps {
  defaultParameters?: SearchParameter[]
  onChange: (values: SearchParameter[]) => void
}

const SearchFormTable: React.FC<SearchTableFormProps> = ({
  defaultParameters = [],
  onChange
}) => {
  const { getFieldsByGroup } = useAppConfig()
  const [query, setQuery] = useState<SearchParameter[]>(defaultParameters)

  const columns: GridColDef[] = getFieldsByGroup('filter').map(
    ({ fieldName, fieldLabel }) => {
      return {
        field: fieldName,
        headerName: fieldLabel,
        align: 'center',
        headerAlign: 'center'
      }
    }
  )

  const onValueChange = (fieldName: string) => {
    return (value: string | Date) => {
      const queryParam = query?.find(param => param.fieldName === fieldName)
      if (queryParam) {
        const newQuery = query.map(queryParms =>
          queryParms.fieldName === fieldName
            ? { ...queryParms, value: value }
            : queryParms
        )
        setQuery(newQuery)
        onChange(newQuery)
      } else {
        const param = { fieldName, value: value, distance: 0 }
        setQuery([...query, param])
        onChange([...query, param])
      }
    }
  }

  const onDistanceChange = (fieldName: string) => {
    return (distance: string) => {
      const queryParam = query?.find(param => param.fieldName === fieldName)
      if (queryParam) {
        const newQuery = query?.map(queryParms =>
          queryParms.fieldName === fieldName
            ? { ...queryParms, distance: parseInt(distance) }
            : queryParms
        )
        setQuery(newQuery)
        onChange(newQuery)
      } else {
        const param = { fieldName, distance: parseInt(distance), value: '' }
        setQuery([...query, param])
        onChange([...query, param])
      }
    }
  }

  const getFieldValue = (fieldName: string) => {
    return query.find(param => param.fieldName === fieldName)?.value
  }
  return (
    <Table aria-label="simple table">
      <TableHead>
        <TableRow>
          <TableCell></TableCell>
          {columns.map(column => (
            <TableCell key={column.field} align={column.align}>
              {column.headerName}
            </TableCell>
          ))}
        </TableRow>
      </TableHead>
      <TableBody>
        <TableRow>
          <TableCell>Type</TableCell>
          {columns.map(column => (
            <TableCell key={column.field} align="left">
              <SelectMatchLevelMenu onChange={onDistanceChange(column.field)} />
            </TableCell>
          ))}
        </TableRow>
        <TableRow>
          <TableCell>Value</TableCell>
          {columns.map(column => (
            <TableCell key={column.field} align="left">
              <TableCellInput
                value={getFieldValue(column.field) || ''}
                onChange={onValueChange(column.field)}
              />
            </TableCell>
          ))}
        </TableRow>
      </TableBody>
    </Table>
  )
}

export default SearchFormTable
