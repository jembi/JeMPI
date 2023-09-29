import { People } from '@mui/icons-material'
import { Container, Divider, Paper, debounce } from '@mui/material'
import { DataGrid, GridFilterModel } from '@mui/x-data-grid'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import ApiClient from '../../services/ApiClient'
import Notification, { Notifications } from '../../types/Notification'
import PageHeader from '../shell/PageHeader'
import React, { useCallback, useState } from 'react'
import dayjs, { Dayjs } from 'dayjs'
import locale from 'dayjs/locale/uk'
import { LocalizationProvider, DesktopDatePicker } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import NOTIFICATIONS_COLUMNS from './notificationsColumns'
import { useNavigate } from 'react-router-dom'
import { encodeQueryString } from 'utils/helpers'

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
    Notifications,
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
          pt: 2,
          mt: 3,
          gap: '10px'
        }}
      >
        <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DesktopDatePicker
            sx={{ mb: '10px' }}
            value={date}
            format="YYYY/MM/DD"
            onChange={value => changeSelectedDate(value)}
            slotProps={{
              textField: {
                variant: 'outlined',
                label: 'Date'
              }
            }}
          />
        </LocalizationProvider>
        <DataGrid
          sx={{
            '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
              outline: 'none'
            }
          }}
          columns={NOTIFICATIONS_COLUMNS}
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
            navigate(
              {
                pathname: 'match-details'
              },
              {
                state: {
                  payload: {
                    notificationId: params.row.id,
                    patient_id: params.row.patient_id,
                    golden_id: params.row.golden_id,
                    score: params.row.score,
                    candidates: params.row.candidates
                  }
                }
              }
            )
          }
        />
      </Paper>
    </Container>
  )
}

export default NotificationWorklist
