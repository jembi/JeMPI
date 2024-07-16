import { People } from '@mui/icons-material'
import {
  Box,
  Button,
  Container,
  Divider,
  Paper,
  Stack,
  TextField,
  TextFieldProps,
  debounce
} from '@mui/material'
import { DataGrid, GridFilterModel, gridClasses } from '@mui/x-data-grid'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import Notification, {
  NotificationState,
  Notifications
} from '../../types/Notification'
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
import SelectDropdown from 'components/shared/SelectDropdown'

const NotificationWorklist = () => {
  const navigate = useNavigate()
  const { apiClient } = useConfig()
  const [selectedStates, setSelectedStates] = useState([NotificationState.OPEN])
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
    items: [{ field: 'state', value: 'OPEN', operator: 'contains' }]
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
      apiClient.fetchMatches(
        paginationModel.pageSize,
        paginationModel.page * paginationModel.pageSize,
        startDateFilter.format('YYYY-MM-DDTHH:mm:ss'),
        endDateFilter.format('YYYY-MM-DDTHH:mm:ss'),
        selectedStates
      ),
    refetchOnWindowFocus: true,
    keepPreviousData: true
  })

  const onFilterChange = useCallback((filterModel: GridFilterModel) => {
    setFilterModel({ ...filterModel })
  }, [])

  return (
    <Container maxWidth={false}>
      <PageHeader
        id="page-header"
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
             < DateTimePicker
            label="Start Date"
            value={startDateFilter}
            onChange={(newValue) => newValue && setStartDateFilter(newValue)}
            slots={{
              textField: (params) => CustomTextField(params, 'start-date-filter'),
            }}
            />
            
            < DateTimePicker
            label="End Date"
            value={endDateFilter}
            onChange={(newValue) => newValue && setEndDateFilter(newValue)}
            slots={{
              textField: (params) => CustomTextField(params, 'end-date-filter'),
            }}
            />
            <SelectDropdown
              listValues={[
                NotificationState.ALL,
                NotificationState.OPEN,
                NotificationState.CLOSED
              ]}
              label="States"
              setSelectedValues={setSelectedStates}
              defaultSelectedValues={[NotificationState.OPEN]}
              multiple={false}
            />
            <Button
              id="filter-button"
              variant="contained"
              onClick={() => refetch()}
              size="large"
            >
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
                onRowDoubleClick={params => {
                  if (params.row.state === NotificationState.CLOSED.toString())
                    return

                  navigate(
                    {
                      pathname: 'match-details'
                    },
                    {
                      state: {
                        payload: {
                          notificationId: params.row.id,
                          notificationType: params.row.type,
                          patient_id: params.row.patient_id,
                          golden_id: params.row.current_golden_id,
                          score: params.row.score,
                          candidates: params.row.candidates
                        }
                      }
                    }
                  )
                }}
              />
            )}
          </Box>
        </Paper>
      </Stack>
    </Container>
  )
}

export default NotificationWorklist;

function CustomTextField(params: TextFieldProps, id: string) {
  return (
      <TextField variant='outlined' 
      label="End Date"
      inputProps={{ id: id }}{...params} />
  );
}