import { People } from '@mui/icons-material'
import { Box, Container, Divider, Paper, debounce } from '@mui/material'
import {
  DataGrid,
  GridColDef,
  GridFilterModel,
  GridRenderCellParams,
  GridValueFormatterParams,
  GridValueGetterParams
} from '@mui/x-data-grid'
import { useNavigate } from '@tanstack/react-location'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import { formatDate, formatName, formatNumber } from 'utils/formatters'
import ApiClient from '../../services/ApiClient'
import Notification from '../../types/Notification'
import PageHeader from '../shell/PageHeader'
import NotificationState from './NotificationState'
import React, { useCallback, useState } from 'react'
import dayjs, { Dayjs } from 'dayjs'
import locale from 'dayjs/locale/uk'
import { LocalizationProvider, DesktopDatePicker } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'

const columns: GridColDef[] = [
  {
    field: 'state',
    headerName: 'Status',
    minWidth: 150,
    align: 'center',
    headerAlign: 'center',
    renderCell: (params: GridRenderCellParams) => {
      return <NotificationState value={params.value || ''} />
    }
  },
  {
    field: 'created_at',
    headerName: 'Date',
    type: 'Date',
    minWidth: 150,
    sortable: true,
    sortingOrder: ['desc'],
    align: 'center',
    headerAlign: 'center',
    valueFormatter: (params: GridValueFormatterParams<Date>) =>
      formatDate(params.value),
    filterable: false
  },
  {
    field: 'patient_id',
    headerName: 'Interaction ID',
    type: 'number',
    minWidth: 150,
    align: 'center',
    headerAlign: 'center',
    filterable: false
  },
  {
    field: 'golden_id',
    headerName: 'Golden ID',
    type: 'number',
    minWidth: 150,
    align: 'center',
    headerAlign: 'center',
    filterable: false
  },
  {
    field: 'score',
    headerName: 'Score',
    type: 'number',
    minWidth: 150,
    align: 'center',
    headerAlign: 'center',
    valueGetter: (params: GridValueGetterParams) => params.row.score,
    valueFormatter: params => formatNumber(params.value),
    filterable: false
  },
  {
    field: 'type',
    headerName: 'Notification Reason',
    minWidth: 150,
    align: 'center',
    filterable: false
  },
  {
    field: 'names',
    headerName: 'Patient',
    minWidth: 150,
    valueFormatter: (params: GridValueFormatterParams<string>) =>
      formatName(params.value),
    filterable: false
  }
]

const NotificationWorklist = () => {
  const navigate = useNavigate()
  const selectedDate = dayjs().locale({
    ...locale
  })
  const [date, setDate] = React.useState(selectedDate)
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 10
  })
  const [filterModel, setFilterModel] = useState<GridFilterModel>({
    items: [{ field: 'state', value: 'New', operator: 'contains' }]
  })
  const { data, error, isLoading, isFetching } = useQuery<
    {
      records: Notification[]
      pagination: {
        total: number
      }
    },
    AxiosError
  >({
    queryKey: [
      'notifications',
      date.format('YYYY-MM-DD'),
      paginationModel.page,
      paginationModel.pageSize,
      filterModel
    ],
    queryFn: () =>
      ApiClient.getMatches(
        paginationModel.pageSize,
        paginationModel.page * paginationModel.pageSize,
        date.format('YYYY-MM-DD'),
        filterModel.items[0].value ? filterModel.items[0].value : ''
      ),
    refetchOnWindowFocus: false
  })

  const onFilterChange = useCallback((filterModel: GridFilterModel) => {
    setFilterModel({ ...filterModel })
  }, [])

  if (isLoading || isFetching) {
    return <Loading />
  }

  if (error) {
    return <ApiErrorMessage error={error} />
  }

  if (!data) {
    return <NotFound />
  }

  const changeSelectedDate = (date: Dayjs | null) => {
    if (date) {
      setDate(date)
    }
  }
  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Notification Worklist'}
        breadcrumbs={[
          {
            link: '/review-matches/',
            title: 'Notifications',
            icon: <People />
          }
        ]}
      />
      <Divider />
      <Paper
        sx={{
          p: 1,
          mt: 4,
          display: 'flex',
          flexDirection: 'column',
          gap: '15px'
        }}
      >
        <Box p={1}>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DesktopDatePicker
              value={date}
              format="YYYY/MM/DD"
              onChange={value => changeSelectedDate(value)}
              slotProps={{
                textField: {
                  variant: 'outlined',
                  label: 'We are looking to name this'
                }
              }}
            />
          </LocalizationProvider>
        </Box>
        <DataGrid
          sx={{
            height: '500px',
            '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
              outline: 'none'
            }
          }}
          columns={columns}
          rows={data.records as Notification[]}
          pageSizeOptions={[10, 25, 50]}
          paginationModel={paginationModel}
          onPaginationModelChange={model => setPaginationModel(model)}
          paginationMode="server"
          rowCount={data.pagination.total || 0}
          filterMode="server"
          filterModel={filterModel}
          onFilterModelChange={debounce(onFilterChange, 3000)}
          onRowDoubleClick={params =>
            navigate({
              to: '/notifications/match-details',
              search: {
                payload: {
                  notificationId: params.row.id,
                  patient_id: params.row.patient_id,
                  golden_id: params.row.golden_id,
                  score: params.row.score,
                  candidates: params.row.candidates
                }
              }
            })
          }
        />
      </Paper>
    </Container>
  )
}

export default NotificationWorklist
