import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Container,
  Divider,
  FormControlLabel,
  Paper,
  Stack,
  Switch,
  Typography
} from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
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
import { LocalizationProvider, DesktopDatePicker } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import dayjs, { Dayjs } from 'dayjs'
import getCellComponent from 'components/shared/getCellComponent'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Search } from '@mui/icons-material'
import { useConfig } from 'hooks/useConfig'

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

  const [searchQuery, setSearchQuery] = useState<Array<SearchParameter>>([])

  const [dateFilter, setDateFilter] = useState(dayjs())

  const [dateSearch, setDateSearch] = useState(dayjs())

  const [searchParams, setSearchParams] = useSearchParams()

  const [isFetchingInteractions, setIsFetchingInteractions] = useState(
    searchParams.get('isFetchingInteractions')
      ? JSON.parse(searchParams.get('isFetchingInteractions') as string)
      : false
  )

  const [filterPayload, setFilterPayload] = useState<FilterQuery>({
    parameters: searchParams.get('parameters')
      ? JSON.parse(searchParams.get('parameters') as string)
      : [],
    limit: searchParams.get('limit')
      ? JSON.parse(searchParams.get('limit') as string)
      : 10,
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
    queryKey: [
      'golden-records',
      JSON.stringify(filterPayload.parameters),
      filterPayload.offset,
      filterPayload.limit,
      filterPayload.sortAsc,
      filterPayload.sortBy
    ],
    queryFn: async () =>
      (await apiClient.searchQuery(
        filterPayload,
        true
      )) as ApiSearchResult<GoldenRecord>,
    refetchOnWindowFocus: false,
    keepPreviousData: true,
    staleTime: 1000 * 60
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
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
        },
        { isFetchingInteractions: isFetchingInteractions } as any
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
    setFilterPayload({
      ...filterPayload,
      parameters: [
        {
          value: dateFilter.toJSON(),
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

  const changeSelectedFileterDate = (date: Dayjs | null) => {
    if (date) {
      setDateFilter(date)
    }
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
        description={'browse through golden records'}
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
                  <DesktopDatePicker
                    value={dateFilter}
                    format="YYYY/MM/DD"
                    onChange={value => changeSelectedFileterDate(value)}
                    slotProps={{
                      textField: {
                        variant: 'outlined',
                        label: 'Date'
                      }
                    }}
                  />
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
        <Accordion>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            <Typography variant="h6">Search within filtered results</Typography>
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
        <Paper sx={{ p: 1 }}>
          <Typography p={1} variant="h6">
            Search result
          </Typography>
          <DataGrid
            sx={{
              '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
                outline: 'none'
              }
            }}
            getRowId={({ uid }) => uid}
            paginationModel={{
              page: filterPayload.offset / filterPayload.limit,
              pageSize: filterPayload.limit
            }}
            columns={columns}
            rows={rows}
            pageSizeOptions={[10, 25, 50, 100]}
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
