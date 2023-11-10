import { People } from '@mui/icons-material'
import {
  Box,
  Button,
  Container,
  Divider,
  Paper,
  Stack,
  debounce
} from '@mui/material'
import { DataGrid, GridFilterModel, gridClasses } from '@mui/x-data-grid'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import Notification, { Notifications } from '../../types/Notification'
import PageHeader from '../shell/PageHeader'
import { useCallback, useState } from 'react'
import dayjs, { Dayjs } from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import NOTIFICATIONS_COLUMNS from './notificationsColumns'
import { useNavigate } from 'react-router-dom'
import { useConfig } from 'hooks/useConfig'
import CustomPagination from 'components/shared/CustomDataGridPagination'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import MultiSelect from 'components/shared/MultiSelect'

const states = ['New', 'Accepted', 'Closed', 'Pending']

const NotificationWorklist = () => {
  const navigate = useNavigate()
  const { apiClient } = useConfig()
  const [selectedStates, setSelectedStates] = useState<string[]>(['New'])
  const [startDateFilter, setStartDateFilter] = useState<Dayjs>(
    dayjs().startOf('day')
  )
  const [endDateFilter, setEndDateFilter] = useState<Dayjs>(
    dayjs().endOf('day')
  )
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 25
  })
  const [filterModel, setFilterModel] = useState<GridFilterModel>({
    items: [{ field: 'state', value: 'New', operator: 'contains' }]
  })

  const { data, error, isLoading, isFetching, refetch } = useQuery<
    Notifications,
    AxiosError
  >({
    queryKey: [
      'notifications',
      paginationModel.page,
      paginationModel.pageSize,
      filterModel
    ],
    queryFn: () =>
      apiClient.getMatches(
        paginationModel.pageSize,
        paginationModel.page * paginationModel.pageSize,
        startDateFilter.format('YYYY-MM-DD HH:mm:ss'),
        endDateFilter.format('YYYY-MM-DD HH:mm:ss'),
        selectedStates
      ),
    refetchOnWindowFocus: false,
    cacheTime: 1000 * 60 * 10,
    keepPreviousData: true,
    staleTime: 1000 * 60 * 10
  })

  const onFilterChange = useCallback((filterModel: GridFilterModel) => {
    setFilterModel({ ...filterModel })
  }, [])

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
        <LocalizationProvider dateAdapter={AdapterDayjs}>
          <Box
            display={'flex'}
            gap={'2rem'}
            alignItems={'center'}
            paddingY={'1rem'}
            flexDirection={{ xs: 'column', md: 'row' }}
          >
            <DateTimePicker
              value={startDateFilter}
              format="YYYY/MM/DD HH:mm:ss"
              onChange={value => value && setStartDateFilter(value)}
              slotProps={{
                textField: {
                  variant: 'outlined',
                  label: 'Start Date'
                }
              }}
            />
            <DateTimePicker
              value={endDateFilter}
              format="YYYY/MM/DD HH:mm:ss"
              onChange={value => value && setEndDateFilter(value)}
              slotProps={{
                textField: {
                  variant: 'outlined',
                  label: 'End Date'
                }
              }}
            />
            <MultiSelect
              listValues={states}
              label="States"
              setSelectedValues={setSelectedStates}
              defaultSelectedValues={['New']}
            />
            <Button variant="contained" onClick={() => refetch()} size="large">
              Filter
            </Button>
          </Box>
        </LocalizationProvider>
        <Paper sx={{ p: 1 }}>
          {error && <ApiErrorMessage error={error} />}
          {!data && !isLoading && !isFetching && <NotFound />}
          <Box
            component={'div'}
            sx={{
              position: 'relative'
            }}
          >
            {data && (
              <DataGrid
                sx={{
                  overflow: 'visible',
                  '& .MuiDataGrid-columnSeparator': {
                    visibility: 'visible'
                  },
                  '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus':
                    {
                      outline: 'none'
                    },
                  [`.${gridClasses.main}`]: {
                    overflow: 'visible'
                  },
                  [`.${gridClasses.columnHeaders}`]: {
                    position: 'sticky',
                    top: 65,
                    zIndex: 1
                  }
                }}
                initialState={{
                  sorting: {
                    sortModel: [{ field: 'created', sort: 'desc' }]
                  }
                }}
                columns={NOTIFICATIONS_COLUMNS}
                rows={data.records as Notification[]}
                slots={{ pagination: CustomPagination }}
                pageSizeOptions={[25, 50, 100]}
                paginationModel={paginationModel}
                onPaginationModelChange={model => setPaginationModel(model)}
                paginationMode="server"
                rowCount={data.pagination.total || 0}
                filterMode="server"
                filterModel={filterModel}
                loading={isLoading}
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
          </Box>
        </Paper>
      </Stack>
    </Container>
  )
}

export default NotificationWorklist
