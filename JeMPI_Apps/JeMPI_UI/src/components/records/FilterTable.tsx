import {
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
import { useState } from 'react'
import DateRangeField from './DateRangeField'
import SelectMatchLevelMenu from './SelectMatchLevelMenu'
import TableCellInput from './TableCellInput'

const FilterTable = () => {
  const { availableFields } = useAppConfig()

  const [filterQuery, setFilterQuery] = useState<{
    [field: string]: { [key: string]: string }
  }>()
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

  const onFilterParamsChange = (key: string) => {
    return (param: string) => {
      return (value: string) => {
        setFilterQuery({
          ...filterQuery,
          [key]: { [param]: value }
        })
      }
    }
  }

  const onDateRangeChange = (key: string, value: string | null) => {
    console.log(key, value)
  }

  return (
    <TableContainer title="Filterby" component={Paper}>
      <Table sx={{ width: '100%' }} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>UID</TableCell>
            <TableCell>Date Created Range</TableCell>
            {columns.map(column => (
              <TableCell align={column.align}>{column.headerName}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow
            sx={{
              '&:last-child td, &:last-child th': { border: 0 }
            }}
          >
            <TableCell>Type</TableCell>
            <TableCell>Date range</TableCell>
            {columns.map(column => (
              <TableCell align="left">
                <SelectMatchLevelMenu
                  onChange={onFilterParamsChange(column.field)}
                />
              </TableCell>
            ))}
          </TableRow>
          <TableRow sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
            <TableCell>Value</TableCell>
            <TableCell>
              <DateRangeField onChange={onDateRangeChange} />
            </TableCell>
            {columns.map(column => (
              <TableCell align="left">
                <TableCellInput onChange={onFilterParamsChange(column.field)} />
              </TableCell>
            ))}
          </TableRow>
        </TableBody>
      </Table>
    </TableContainer>
  )
}

export default FilterTable
