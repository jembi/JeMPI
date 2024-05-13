import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Container,
  Divider,
  FormControlLabel,
  Paper,
  Stack,
  Switch,
  Typography
} from '@mui/material'
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  gridClasses
} from '@mui/x-data-grid'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import { useAppConfig } from 'hooks/useAppConfig'
import {
  AnyRecord,
  GoldenRecord,
  PatientRecord,
  ValueOf
} from 'types/PatientRecord'
import { FilterTable } from './FilterTable'
import {
  ApiSearchResult,
  FilterQuery,
  SearchParameter
} from 'types/SimpleSearch'
import { useEffect, useMemo, useState } from 'react'
import { isPatientCorresponding } from 'hooks/useSearch'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import PageHeader from 'components/shell/PageHeader'
import {
  LocalizationProvider,
  DesktopDatePicker,
  DateTimePicker
} from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import dayjs, { Dayjs } from 'dayjs'
import getCellComponent from 'components/shared/getCellComponent'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Search } from '@mui/icons-material'
import { useConfig } from 'hooks/useConfig'
import CustomPagination from 'components/shared/CustomDataGridPagination'

// TODO: Later -  We can update this at a later stage, such the field configuration info can contain the getAlignment, since this can be dynamic
const getAlignment = (fieldName: string) =>
  fieldName === 'givenName' ||
  fieldName === 'familyName' ||
  fieldName === 'city' ||
  fieldName === 'gender'
    ? 'left'
    : 'center'

const Records = () => {
  const navigate = useNavigate()
  const { apiClient } = useConfig()
  const { getFieldsByGroup } = useAppConfig()
  const [startDateFilter, setStartDateFilter] = useState<Dayjs>(
    dayjs().startOf('day')
  )
  const [endDateFilter, setEndDateFilter] = useState<Dayjs>(
    dayjs().endOf('day')
  )
  const [searchQuery, setSearchQuery] = useState<Array<SearchParameter>>([])
  const [dateSearch, setDateSearch] = useState(dayjs())
  const [searchParams, setSearchParams] = useSearchParams()
  const [isFetchingInteractions, setIsFetchingInteractions] = useState<boolean>(
    searchParams.get('isFetchingInteractions')
      ? JSON.parse(searchParams.get('isFetchingInteractions') as string) ==
          'true'
      : false
  )

  const [filterPayload, setFilterPayload] = useState<FilterQuery>({
    parameters: searchParams.get('parameters')
      ? JSON.parse(searchParams.get('parameters') as string)
      : [],
    limit: searchParams.get('limit')
      ? JSON.parse(searchParams.get('limit') as string)
      : 25,
    offset: searchParams.get('offset')
      ? JSON.parse(searchParams.get('offset') as string)
      : 0,
    sortAsc: searchParams.get('')
      ? JSON.parse(searchParams.get('sortAsc') as string)
      : 0,
    sortBy: searchParams.get('sortBy')
      ? JSON.parse(searchParams.get('sortBy') as string)
      : 'auxDateCreated'
  })

  const columns: GridColDef[] = getFieldsByGroup('linked_records').map(
    ({ fieldName, fieldLabel, formatValue, getValue }) => {
      return {
        field: fieldName,
        headerName: fieldLabel,
        flex: fieldName === 'sourceId' ? 2 : 1,
        valueFormatter: ({ value }: { value: ValueOf<AnyRecord> }) =>
          formatValue(value),
        sortable: false,
        valueGetter: getValue,
        disableColumnMenu: true,
        align: getAlignment(fieldName),
        headerAlign: getAlignment(fieldName),
        filterable: false,
        headerClassName: 'super-app-theme--header',
        renderCell: (params: GridRenderCellParams) =>
          getCellComponent(fieldName, params)
      }
    }
  )

  const { data, isError, error, isLoading } = useQuery<
    ApiSearchResult<GoldenRecord>,
    AxiosError
  >({
    queryKey: ['golden-records', JSON.stringify(filterPayload)],
    queryFn: async () =>
      (await apiClient.searchQuery(
        filterPayload
      )) as ApiSearchResult<GoldenRecord>,
    refetchOnWindowFocus: false,
    keepPreviousData: true
  })

  const rows = useMemo(() => {
    if (!data) {
      return []
    }
    return !isFetchingInteractions
      ? data.records.data
      : data.records.data.reduce((acc: Array<AnyRecord>, record) => {
          acc.push({ ...record, type: 'Current' }, ...record.linkRecords)
          return acc
        }, [])
  }, [isFetchingInteractions, data])
  useEffect(() => {
    setSearchParams(
      Object.entries(filterPayload).reduce(
        (acc, [k, v]) => {
          acc[k] = JSON.stringify(v)
          return acc
        },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        { isFetchingInteractions } as any
      )
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterPayload])

  if (isError) {
    return <ApiErrorMessage error={error} />
  }

  const onSearch = (query: SearchParameter[]) => {
    setSearchQuery(query)
  }

  const onFilter = (query: SearchParameter[]) => {
    const startDate = startDateFilter.toJSON()
    const endDate = endDateFilter.toJSON()
    setFilterPayload({
      ...filterPayload,
      parameters: [
        {
          value: `${startDate}_${endDate}`,
          distance: -1,
          fieldName: 'auxDateCreated'
        },
        ...query
      ]
    })
  }

  const getClassName = (patient: PatientRecord) => {
    return isPatientCorresponding(patient, searchQuery)
      ? `super-app-theme--searchable`
      : ''
  }

  const changeSelectedSearchDate = (date: Dayjs | null) => {
    if (date) {
      setDateSearch(date)
    }
  }

  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Browse Patients'}
        breadcrumbs={[
          {
            link: '/browse-records/',
            title: 'Browse',
            icon: <Search />
          }
        ]}
      />
      <Divider />

      <Stack padding={'2rem 1rem 1rem 1rem'} gap="10px" flexDirection="column">
        <Accordion>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            <Typography variant="h6">Filter by</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack gap="10px">
              <Stack gap="20px" flexDirection="row">
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
                  </Box>
                </LocalizationProvider>
                <FormControlLabel
                  control={
                    <Switch
                      checked={isFetchingInteractions}
                      onChange={(_e, checked) =>
                        setIsFetchingInteractions(checked)
                      }
                    />
                  }
                  label="Get Interactions"
                />
              </Stack>
              <FilterTable
                defaultParameters={filterPayload.parameters}
                searchButtonLabel="Filter"
                onSubmit={onFilter}
                onCancel={() =>
                  setFilterPayload({
                    ...filterPayload,
                    parameters: []
                  })
                }
              />
            </Stack>
          </AccordionDetails>
        </Accordion>
        {/* Search will be refactored in the future to be part of filter */}
        {false && (
          <Accordion>
            <AccordionSummary
              expandIcon={<ExpandMoreIcon />}
              aria-controls="panel1a-content"
              id="panel1a-header"
            >
              <Typography variant="h6">
                Search within filtered results
              </Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Stack gap="10px" alignItems="flex-start">
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DesktopDatePicker
                    value={dateSearch}
                    format="YYYY/MM/DD"
                    onChange={value => changeSelectedSearchDate(value)}
                    slotProps={{
                      textField: {
                        variant: 'outlined',
                        label: 'Date'
                      }
                    }}
                  />
                </LocalizationProvider>

                <FilterTable
                  onSubmit={onSearch}
                  onCancel={() => setSearchQuery([])}
                />
              </Stack>
            </AccordionDetails>
          </Accordion>
        )}
        <Paper sx={{ p: 1 }}>
          <Typography p={1} variant="h6">
            Search result
          </Typography>
          <DataGrid
            sx={{
              overflow: 'visible',
              '& .MuiDataGrid-columnSeparator': {
                visibility: 'visible'
              },
              '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
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
            getRowId={({ uid }) => uid}
            paginationModel={{
              page: filterPayload.offset / filterPayload.limit,
              pageSize: filterPayload.limit
            }}
            slots={{ pagination: CustomPagination }}
            columns={columns}
            rows={rows}
            pageSizeOptions={[25, 50, 100]}
            onRowDoubleClick={params => {
              if ('linkRecords' in params.row) {
                navigate({
                  pathname: `/record-details/${params.row.uid}`
                })
              }
            }}
            getRowClassName={params =>
              `${
                params.row.type === 'Current' && isFetchingInteractions
                  ? 'super-app-theme--Current'
                  : getClassName(params.row)
              }`
            }
            onPaginationModelChange={({ page, pageSize }) =>
              setFilterPayload({
                ...filterPayload,
                offset: page * filterPayload.limit,
                limit: pageSize
              })
            }
            paginationMode="server"
            loading={isLoading}
            rowCount={data?.records.pagination?.total || 0}
          />
        </Paper>
      </Stack>
    </Container>
  )
}

export default Records
