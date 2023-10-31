import { People } from '@mui/icons-material'
import { Box, Container, Divider, Paper, Stack, debounce } from '@mui/material'
import { DataGrid, GridFilterModel } from '@mui/x-data-grid'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import Notification, { Notifications } from '../../types/Notification'
import PageHeader from '../shell/PageHeader'
import React, { useCallback, useState } from 'react'
import dayjs, { Dayjs } from 'dayjs'
import locale from 'dayjs/locale/uk'
import { LocalizationProvider, DesktopDatePicker } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import NOTIFICATIONS_COLUMNS from './notificationsColumns'
import { useNavigate } from 'react-router-dom'
import { useConfig } from 'hooks/useConfig'

const NotificationWorklist = () => {
  const { apiClient } = useConfig()

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
      apiClient.getMatches(
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
            link: '/notifications/',
            title: 'Notifications',
            icon: <People />
          }
        ]}
      />
      <Divider />
      <Stack padding={'2rem 1rem 1rem 1rem'}>
        <Box>
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
        </Box>
        <Paper sx={{ p: 1 }}>
          {error && <ApiErrorMessage error={error} />}
          {!data && <NotFound />}
          {data && (
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
              loading={isLoading || isFetching}

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
          )}
        </Paper>
      </Stack>
    </Container>
  )
}

export default NotificationWorklist
