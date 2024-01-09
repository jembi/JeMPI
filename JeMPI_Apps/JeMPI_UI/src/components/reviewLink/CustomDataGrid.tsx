import {
  DataGridProps,
  GridCellParams,
  GridColDef,
  GridRenderCellParams,
  GridValidRowModel,
  GridValueFormatterParams,
  DataGrid as MuiDataGrid
} from '@mui/x-data-grid'
import { FieldGroup } from 'types/Fields'
import { AnyRecord, ValueOf } from 'types/PatientRecord'
import { useAppConfig } from '../../hooks/useAppConfig'
import getCellComponent from 'components/shared/getCellComponent'
import { useMemo } from 'react'

type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>
type PartialBy<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>

interface CustomDataGridProps<R extends GridValidRowModel> extends PartialBy<DataGridProps<R>, 'columns'> {
  action?: (uid: string) => void
}

const getRecordTypeClassName = (params: GridCellParams) => {
  return params.row.type === 'Current' ? 'record-type' : ''
}

const getCellClassName = (
  params: GridCellParams,
  groups: FieldGroup[],
  data: AnyRecord
) => {
  if (groups.includes('demographics')) {
    return params.value === data.demographicData[params.field]
      ? 'matching-cell'
      : ''
  } else return ''
}

const CustomDataGrid = <R extends GridValidRowModel>({
  sx,
  rows,
  action,
  ...props
}: CustomDataGridProps<R>) => {
  const { availableFields } = useAppConfig()

  const fieldColumns: GridColDef[] = availableFields.map(
    ({ fieldName, fieldLabel, groups, formatValue, getValue }) => ({
      field: fieldName,
      headerName: fieldLabel,
      flex: fieldName === 'sourceId' ? 2 : 1,
      sortable: true,
      filterable: true,
      align: 'left',
      headerAlign: 'left',
      headerClassName: 'super-app-theme--linkHeader',
      valueGetter: getValue,
      valueFormatter: (params: GridValueFormatterParams<ValueOf<AnyRecord>>) =>
        formatValue(params.value),
      cellClassName: (params: GridCellParams) =>
        fieldName === 'recordType'
          ? getRecordTypeClassName(params)
          : getCellClassName(params, groups, rows[0] as any as AnyRecord),
      renderCell: (params: GridRenderCellParams) =>
        getCellComponent(fieldName, params)
    })
  )

  const columns: GridColDef[] = useMemo(
    () =>
      action
        ? [
            ...fieldColumns,
            {
              field: 'action',
              type: 'action',
              headerName: 'Action',
              flex: 1,
              sortable: false,
              filterable: false,
              align: 'center',
              headerAlign: 'center',
              headerClassName: 'super-app-theme--linkHeader',
              renderCell: (params: GridRenderCellParams) =>
                getCellComponent('actions', params, () => {
                  if (action) action(params.row.uid)
                })
            }
          ]
        : fieldColumns,
    [action, fieldColumns]
  )

  return (
    <MuiDataGrid
      autoHeight={true}
      disableColumnMenu={true}
      getRowId={({ uid }) => uid}
      hideFooter
      sx={{
        '& .MuiDataGrid-columnHeaders': {
          borderRadius: '0px'
        },
        '& .matching-cell': {
          fontWeight: 'bold'
        },
        ...sx
      }}
      rows={rows}
      initialState={{
        columns: {
          columnVisibilityModel: {
            actions: !!action
          }
        }
      }}
      {...{ ...props, columns: columns }}
    />
  )
}

export default CustomDataGrid
