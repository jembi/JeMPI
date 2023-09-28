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
import {
  DataGrid,
  GridColDef,
  GridPaginationModel,
  GridRenderCellParams
} from '@mui/x-data-grid'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import { useAppConfig } from 'hooks/useAppConfig'
import { AnyRecord, PatientRecord, ValueOf } from 'types/PatientRecord'
import { FilterTable } from './FilterTable'
import { FilterQuery, SearchParameter } from 'types/SimpleSearch'
import { useState } from 'react'
import { isPatientCorresponding } from 'hooks/useSearch'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import ApiClient from 'services/ApiClient'
import { useNavigate } from '@tanstack/react-location'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import PageHeader from 'components/shell/PageHeader'
import { LocalizationProvider, DesktopDatePicker } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import dayjs, { Dayjs } from 'dayjs'
import getCellComponent from 'components/shared/getCellComponent'

const getAlignment = (fieldName: string) =>
  fieldName === 'givenName' ||
  fieldName === 'familyName' ||
  fieldName === 'city' ||
  fieldName === 'gender'
    ? 'left'
    : 'center'

const Records = () => {
  const navigate = useNavigate()
  const { getFieldsByGroup } = useAppConfig()

  const [isFetchingInteractions, setIsFetchingInteractions] = useState(false)
  const [searchQuery, setSearchQuery] = useState<Array<SearchParameter>>([])
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 10
  })

  const [dateFilter, setDateFilter] = useState(dayjs())

  const [dateSearch, setDateSearch] = useState(dayjs())

  const [filterPayload, setFilterPayload] = useState<FilterQuery>({
    parameters: [],
    limit: 1000,
    offset: 0,
    sortAsc: false,
    sortBy: 'auxDateCreated'
  })

  const [goldenIds, setGoldenIds] = useState<Array<string>>([])

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

  const goldenIdsQuery = useQuery<
    { data: Array<string>; pagination: { total: number } },
    AxiosError
  >({
    queryKey: [
      'golden-records-ids',
      ...filterPayload.parameters,
      filterPayload.createdAt,
      filterPayload.offset,
      filterPayload.limit,
      isFetchingInteractions
    ],
    queryFn: async () =>
      isFetchingInteractions
        ? await ApiClient.getFilteredGoldenIdsWithInteractionCount(
            filterPayload
          )
        : await ApiClient.getFilteredGoldenIds(filterPayload),
    onSuccess: data => {
      if (filterPayload.offset > 0) {
        setGoldenIds([...goldenIds, ...data.data])
      } else {
        setGoldenIds([...data.data])
      }
    },
    refetchOnWindowFocus: false
  })

  const expandeGoldenRecordsQuery = useQuery<Array<AnyRecord>, AxiosError>({
    queryKey: [
      'expanded-golden-records',
      paginationModel.page,
      paginationModel.pageSize,
      ...filterPayload.parameters,
      goldenIds?.slice(
        paginationModel.page * paginationModel.pageSize,
        paginationModel.page * paginationModel.pageSize +
          paginationModel.pageSize
      ),
      isFetchingInteractions
    ],
    queryFn: async () =>
      isFetchingInteractions
        ? await ApiClient.getFlatExpandedGoldenRecords(
            goldenIds?.slice(
              paginationModel.page * paginationModel.pageSize,
              paginationModel.page * paginationModel.pageSize +
                paginationModel.pageSize
            )
          )
        : await ApiClient.getExpandedGoldenRecords(
            goldenIds?.slice(
              paginationModel.page * paginationModel.pageSize,
              paginationModel.page * paginationModel.pageSize +
                paginationModel.pageSize
            )
          ),
    enabled: goldenIds.length > 0,
    refetchOnWindowFocus: false
  })

  if (expandeGoldenRecordsQuery.isError) {
    return <ApiErrorMessage error={expandeGoldenRecordsQuery.error} />
  }

  const onSearch = (query: SearchParameter[]) => {
    setSearchQuery(query)
  }

  const onFilter = (query: SearchParameter[]) => {
    setFilterPayload({
      ...filterPayload,
      parameters: [...query],
      createdAt: dateFilter.toJSON()
    })
  }

  // This funciton needs to be removed when the @mui/x-data-grid is
  // replaced with the the Material react table (https://www.material-react-table.com/)
  const handlePagination = (model: GridPaginationModel) => {
    setPaginationModel(model)
    if (
      filterPayload.offset === 0 &&
      paginationModel.pageSize * paginationModel.page +
        paginationModel.pageSize >=
        filterPayload.limit
    ) {
      setFilterPayload({
        ...filterPayload,
        offset: filterPayload.offset + filterPayload.limit
      })
    }
    if (
      filterPayload.offset !== 0 &&
      paginationModel.pageSize * paginationModel.page +
        paginationModel.pageSize <
        filterPayload.limit
    ) {
      setFilterPayload({
        ...filterPayload,
        offset: filterPayload.offset - filterPayload.limit
      })
    }
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
        title={`Browse Patients`}
        description={'browse through golden records'}
      />
      <Divider />
      <Stack mt="20px" gap="10px" flexDirection="column">
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
                searchButtonLabel="Filter"
                onSubmit={onFilter}
                onCancel={() =>
                  setFilterPayload({
                    ...filterPayload,
                    parameters: [],
                    createdAt: dayjs(new Date()).format('YYYY-MM-DD')
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
            paginationModel={paginationModel}
            columns={columns}
            rows={expandeGoldenRecordsQuery?.data || []}
            pageSizeOptions={[10, 25, 50, 100]}
            onRowDoubleClick={params =>
              navigate({
                to: `record-details/${params.row.uid}`
              })
            }
            getRowClassName={params =>
              `${
                params.row.type === 'Current' && isFetchingInteractions
                  ? 'super-app-theme--Current'
                  : getClassName(params.row)
              }`
            }
            onPaginationModelChange={handlePagination}
            paginationMode="server"
            loading={expandeGoldenRecordsQuery.isLoading}
            rowCount={goldenIdsQuery?.data?.pagination?.total || 0}
          />
        </Paper>
      </Stack>
    </Container>
  )
}

export default Records
