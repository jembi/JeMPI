import {
  GridColDef,
  GridRenderCellParams,
  GridValueGetterParams,
  GridValueFormatterParams
} from '@mui/x-data-grid'
import getCellComponent from 'components/shared/getCellComponent'
import NotificationState from './NotificationState'
import { formatNumber, formatName } from 'utils/formatters'

const NOTIFICATIONS_COLUMNS: GridColDef[] = [
  {
    field: 'state',
    headerName: 'Status',
    flex: 1,
    align: 'center',
    headerClassName: 'super-app-theme--header',
    renderCell: (params: GridRenderCellParams) => {
      return <NotificationState value={params.value || ''} />
    }
  },
  {
    field: 'created',
    headerName: 'Date',
    type: 'date',
    flex: 1,
    sortable: true,
    sortingOrder: ['desc'],
    filterable: false,
    headerClassName: 'super-app-theme--header',
    renderCell: (params: GridRenderCellParams) =>
      getCellComponent('createdAt', params)
  },
  {
    field: 'patient_id',
    headerName: 'Interaction ID',
    type: 'number',
    flex: 1,
    headerClassName: 'super-app-theme--header',
    filterable: false
  },
  {
    field: 'golden_id',
    headerName: 'Golden ID',
    type: 'number',
    flex: 1,
    headerClassName: 'super-app-theme--header',
    filterable: false
  },
  {
    field: 'score',
    headerName: 'Score',
    type: 'number',
    flex: 1,
    headerClassName: 'super-app-theme--header',
    valueGetter: (params: GridValueGetterParams) => params.row.score,
    valueFormatter: params => formatNumber(params.value),
    filterable: false
  },
  {
    field: 'type',
    headerName: 'Notification Reason',
    flex: 1,
    headerClassName: 'super-app-theme--header',
    filterable: false
  },
  {
    field: 'names',
    headerName: 'Patient',
    headerClassName: 'super-app-theme--header',
    flex: 2,
    valueFormatter: (params: GridValueFormatterParams<string>) =>
      formatName(params.value),
    filterable: false
  }
]

export default NOTIFICATIONS_COLUMNS
