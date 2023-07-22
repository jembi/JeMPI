import { Link, SxProps, Theme, Typography } from '@mui/material'
import {
  GridCellParams,
  GridColDef,
  GridRenderCellParams,
  GridValueFormatterParams,
  GridValueGetterParams,
  DataGrid as MuiDataGrid
} from '@mui/x-data-grid'
import { DisplayField } from 'types/Fields'
import { AnyRecord } from 'types/PatientRecord'
import { useAppConfig } from '../../hooks/useAppConfig'
import SourceIdComponent from 'components/browseRecords/SourceIdComponent'
import MoreIcon from './MoreIcon'
interface DataGridProps {
  data: AnyRecord[]
  onLinkedRecordDialogOpen?: (uid: string) => void
  onNewGoldenRecordDialogOpen?: (uid: string) => void
  hideAction?: boolean
  isLoading?: boolean
  sx?: SxProps<Theme>
}
const getRecordTypeClassName = (params: GridCellParams) => {
  return params.row === 'Golden' ? 'record-type' : ''
}

const getCellClassName = (
  params: GridCellParams,
  field: DisplayField,
  data: AnyRecord
) => {
  if (field.groups.includes('demographics')) {
    return params.value === data[params.field] ? 'matching-cell' : ''
  } else return ''
}

const DataGrid: React.FC<DataGridProps> = ({
  data,
  onLinkedRecordDialogOpen,
  isLoading = false,
  sx
}) => {
  const { availableFields } = useAppConfig()

  const columns: GridColDef[] = availableFields.map(field => {
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
          cellClassName: (params: GridCellParams) =>
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
          flex: fieldName === 'sourceId' ? 2 : 1,
          valueFormatter: (
            params: GridValueFormatterParams<number | string | Date>
          ) => formatValue(params.value),
          cellClassName: (params: GridCellParams) =>
            getCellClassName(params, field, data[0]),
          renderCell: (params: GridRenderCellParams) => {
            if (fieldName === 'sourceId') {
              return <SourceIdComponent content={params.row.sourceId} />
            }
          }
        }
    }
  })
  columns.push({
    field: 'actions',
    headerName: 'Actions',
    maxWidth: 180,
    minWidth: 120,
    flex: 1,
    align: 'center',
    headerAlign: 'center',
    sortable: false,
    filterable: false,
    valueGetter: (params: GridValueGetterParams) => ({
      id: params.row.id,
      patient: params.row.patient,
      type: params.row.type
    }),
    renderCell: (params: GridRenderCellParams) => {
      switch (params.row.type) {
        case 'Current':
        case 'Golden':
          return <MoreIcon params={params} />
        case 'Candidate':
          return (
            <Link
              sx={{ ':hover': { cursor: 'pointer' } }}
              onClick={() =>
                onLinkedRecordDialogOpen
                  ? onLinkedRecordDialogOpen(params.row.uid)
                  : null
              }
            >
              Link
            </Link>
          )
        default:
          return <></>
      }
    }
  })

  return (
    <MuiDataGrid
      columns={columns}
      pageSizeOptions={[10]}
      getRowId={({ uid }) => uid}
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
      rows={data}
    />
  )
}

export default DataGrid
