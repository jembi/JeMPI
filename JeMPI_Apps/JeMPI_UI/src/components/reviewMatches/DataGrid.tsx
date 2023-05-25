import { SxProps, Theme, Typography } from '@mui/material'
import {
  GridCellParams,
  GridColumns,
  GridRenderCellParams,
  GridValueFormatterParams,
  DataGrid as MuiDataGrid
} from '@mui/x-data-grid'
import { DisplayField } from 'types/Fields'
import { AnyRecord } from 'types/PatientRecord'
import { useAppConfig } from '../../hooks/useAppConfig'
interface DataGridProps {
  data: AnyRecord[]
  onLinkedRecordDialogOpen?: (uid: string) => void
  onNewGoldenRecordDialogOpen?: (uid: string) => void
  hideAction?: boolean
  isLoading?: boolean
  sx?: SxProps<Theme>
}
const getRecordTypeClassName = (params: GridCellParams<string>) => {
  return params.row.type === 'Golden' ? 'record-type' : ''
}

const getCellClassName = (
  params: GridCellParams<string>,
  field: DisplayField,
  data: AnyRecord
) => {
  if (field.groups.includes('demographics')) {
    return params.value === data[params.field] ? 'matching-cell' : ''
  } else return ''
}

const DataGrid: React.FC<DataGridProps> = ({ data, isLoading = false, sx }) => {
  const { availableFields } = useAppConfig()

  const columns: GridColumns = [
    ...availableFields.map(field => {
      const { fieldName, fieldLabel, formatValue } = field
      switch (fieldName) {
        case 'recordType':
          return {
            field: fieldName,
            headerName: fieldLabel,
            flex: 1,
            valueFormatter: (
              params: GridValueFormatterParams<number | string | Date>
            ) => formatValue(params.value),
            cellClassName: (params: GridCellParams<string>) =>
              getRecordTypeClassName(params),
            renderCell: (params: GridRenderCellParams) => {
              switch (params.row.type) {
                case 'Current':
                  return <Typography>Patient</Typography>
                case 'Golden':
                  return (
                    <Typography color="#D79B01" fontWeight={700}>
                      Golden
                    </Typography>
                  )
                case 'Candidate':
                  if (params.row.searched) {
                    return <Typography>Searched</Typography>
                  } else {
                    return <Typography>Blocked</Typography>
                  }
                default:
                  return <></>
              }
            }
          }
        default:
          return {
            field: fieldName,
            headerName: fieldLabel,
            flex: 1,
            valueFormatter: (
              params: GridValueFormatterParams<number | string | Date>
            ) => formatValue(params.value),
            cellClassName: (params: GridCellParams<string>) =>
              getCellClassName(params, field, data[0])
          }
      }
    })
  ]

  return (
    <MuiDataGrid
      columns={columns}
      rows={data}
      pageSize={10}
      rowsPerPageOptions={[10]}
      getRowId={row => row.uid}
      hideFooter
      loading={isLoading}
      sx={{
        '.MuiDataGrid-root': {
          borderRadius: '50px'
        },
        '& .current-patient-cell': {
          color: '#7B61FF'
        },
        '& .golden-patient-cell': {
          color: '#D79B01'
        },
        '& .matching-cell': {
          fontWeight: 'bold'
          // border: '1px dotted red'
        },
        '& .record-type': {
          borderLeft: '4px solid #D79B01'
        },
        ...sx
      }}
      autoHeight={true}
    />
  )
}

export default DataGrid
