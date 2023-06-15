import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow
} from '@mui/material'
import { GridColDef } from '@mui/x-data-grid'
import { useAppConfig } from 'hooks/useAppConfig'
import { FC, useState } from 'react'
import SelectMatchLevelMenu from './SelectMatchLevelMenu'
import TableCellInput from './TableCellInput'
import { SearchParameter } from 'types/SimpleSearch'

export type QueryParam = {
  [field: string]: { [key: string]: string }
}

export const FilterTable: FC<{
  onSubmit: (query: SearchParameter[]) => void
  onCancel: () => void
}> = ({ onSubmit, onCancel }) => {
  const { availableFields } = useAppConfig()

  const [query, setQuery] = useState<SearchParameter[]>([])
  const columns: GridColDef[] = availableFields.map(
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
      } else {
        const param = { fieldName, value: value, distance: 0 }
        setQuery([...query, param])
      }
    }
  }

  const onDistanceChange = (fieldName: string) => {
    return (distance: number) => {
      const queryParam = query?.find(param => param.fieldName === fieldName)
      if (queryParam) {
        const newQuery = query?.map(queryParms =>
          queryParms.fieldName === fieldName
            ? { ...queryParms, distance: distance }
            : queryParms
        )
        setQuery(newQuery)
      } else {
        const param = { fieldName, distance: distance, value: '' }
        setQuery([...query, param])
      }
    }
  }

  const getFieldValue = (fieldName: string) => {
    return query.find(param => param.fieldName === fieldName)?.value
  }

  const getMatchLevel = (fieldName: string) => {
    return query.find(param => param.fieldName === fieldName)?.distance
  }

  const handleCancel = () => {
    setQuery([])
    onCancel()
  }

  return (
    <TableContainer component={Paper}>
      <Table aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            {columns.map(column => (
              <TableCell align={column.align}>{column.headerName}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            <TableCell>Type</TableCell>
            {columns.map(column => (
              <TableCell align="left">
                <SelectMatchLevelMenu
                  value={getMatchLevel(column.field) || NaN}
                  onChange={onDistanceChange(column.field)}
                />
              </TableCell>
            ))}
          </TableRow>
          <TableRow>
            <TableCell>Value</TableCell>
            {columns.map(column => (
              <TableCell align="left">
                <TableCellInput
                  value={getFieldValue(column.field) || ''}
                  onChange={onValueChange(column.field)}
                />
              </TableCell>
            ))}
          </TableRow>
        </TableBody>
      </Table>
      <Box p={3} display={'flex'} justifyContent={'flex-end'} gap={'10px'}>
        <Button onClick={() => handleCancel()}>Cancel</Button>
        <Button onClick={() => onSubmit(query)}>Search</Button>
      </Box>
    </TableContainer>
  )
}
