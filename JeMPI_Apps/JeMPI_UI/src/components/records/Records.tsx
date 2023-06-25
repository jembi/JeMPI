import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Container,
  Divider,
  Paper,
  TextField,
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
import {
  AnyRecord,
  GoldenRecord,
  PatientRecord,
  ValueOf
} from 'types/PatientRecord'
import { FilterTable } from './FilterTable'
import { SearchParameter } from 'types/SimpleSearch'
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
import locale from 'dayjs/locale/uk'

const getAlignment = (fieldName: string) =>
  fieldName === 'givenName' ||
  fieldName === 'familyName' ||
  fieldName === 'city' ||
  fieldName === 'gender'
    ? 'left'
    : fieldName === 'dob'
    ? 'right'
    : 'center'

const Records = () => {
  const navigate = useNavigate()
  const { getFieldsByGroup } = useAppConfig()
  const [searchQuery, setSearchQuery] = useState<Array<SearchParameter>>([])
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 100
  })
  const [goldenIdsPagination, setGoldenIdsPagination] = useState({
    offset: 0,
    limit: 1000
  })

  const [dateFilter, setDateFilter] = useState(
    dayjs().locale({
      ...locale
    })
  )

  const [dateSearch, setDateSearch] = useState(
    dayjs().locale({
      ...locale
    })
  )

  const [goldenIds, setGoldenIds] = useState<Array<string>>([])

  const columns: GridColDef[] = getFieldsByGroup('linked_records').map(
    ({ fieldName, fieldLabel, formatValue }) => {
      return {
        field: fieldName,
        headerName: fieldLabel,
        flex: 1,
        valueFormatter: ({ value }: { value: ValueOf<AnyRecord> }) =>
          formatValue(value),
        sortable: false,
        disableColumnMenu: true,
        align: getAlignment(fieldName),
        headerAlign: getAlignment(fieldName),
        filterable: false,
        headerClassName: 'super-app-theme--header',
        renderCell: (params: GridRenderCellParams) => {
          if (fieldName === 'sourceId') {
            return (
              <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                {params.row.sourceId.map((value: any) => (
                  <Typography fontSize={'9px'}>{value.facility}</Typography>
                ))}
              </Box>
            )
          }
        }
      }
    }
  )

  const goldenIdsQuery = useQuery<Array<string>, AxiosError>({
    queryKey: ['golden-records-ids', goldenIdsPagination.offset],
    queryFn: async () =>
      await ApiClient.getFilteredGoldenIds(
        goldenIdsPagination.offset,
        goldenIdsPagination.limit
      ),
    onSuccess: data => {
      if (goldenIds.length === 0 || goldenIdsPagination.offset === 0) {
        setGoldenIds([...data])
      } else if (goldenIdsPagination.offset > 0) {
        setGoldenIds([...goldenIds, ...data])
      }
    },
    refetchOnWindowFocus: false
  })

  const expandeGoldenRecordsQuery = useQuery<Array<GoldenRecord>, AxiosError>({
    queryKey: [
      'expanded-golden-records',
      paginationModel.page,
      paginationModel.pageSize
    ],
    queryFn: async () =>
      (await ApiClient.getExpandedGoldenRecords(
        goldenIds?.slice(
          paginationModel.page * paginationModel.pageSize,
          paginationModel.page * paginationModel.pageSize +
            paginationModel.pageSize
        ),
        false
      )) as Array<GoldenRecord>,
    enabled: goldenIds.length != 0,
    refetchOnWindowFocus: false
  })

  if (expandeGoldenRecordsQuery.isError) {
    return <ApiErrorMessage error={expandeGoldenRecordsQuery.error} />
  }

  const onSearch = (query: SearchParameter[]) => {
    setSearchQuery(query)
  }

  const handlePagination = (model: GridPaginationModel) => {
    setPaginationModel(model)
    if (
      goldenIdsPagination.offset === 0 &&
      paginationModel.pageSize * paginationModel.page +
        paginationModel.pageSize >=
        goldenIdsPagination.limit - paginationModel.pageSize
    ) {
      if (goldenIdsQuery.data) {
        setGoldenIds([...goldenIds, ...goldenIdsQuery.data])
      }
      setGoldenIdsPagination({
        ...goldenIdsPagination,
        offset:
          goldenIdsPagination.offset +
          (goldenIdsPagination.limit - paginationModel.pageSize)
      })
    }
    if (
      goldenIdsPagination.offset !== 0 &&
      paginationModel.pageSize * paginationModel.page +
        paginationModel.pageSize <
        goldenIdsPagination.limit - paginationModel.pageSize
    ) {
      setGoldenIdsPagination({
        ...goldenIdsPagination,
        offset:
          goldenIdsPagination.offset -
          (goldenIdsPagination.limit - paginationModel.pageSize)
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
      <Box
        sx={{
          mt: '20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px'
        }}
      >
        <Accordion>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            <Typography variant="h6">Filter by</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Box sx={{ p: 1, display: 'flex', gap: '10px' }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DesktopDatePicker
                  value={dateFilter}
                  format="YYYY/MM/DD"
                  onChange={value => changeSelectedFileterDate(value)}
                  slotProps={{
                    textField: {
                      variant: 'outlined',
                      label: 'We are looking to name this'
                    }
                  }}
                />
              </LocalizationProvider>
              <TextField
                title="Limit"
                helperText="how many records you want to fetch ?"
                value={goldenIdsPagination}
                onChange={e =>
                  setGoldenIdsPagination({
                    ...goldenIdsPagination,
                    limit: parseInt(e.target.value)
                  })
                }
                type="number"
                inputProps={{ inputMode: 'numeric', pattern: '[0-9]*' }}
              />
            </Box>
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
            <Box sx={{ p: 1, display: 'flex', gap: '10px' }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DesktopDatePicker
                  value={dateSearch}
                  format="YYYY/MM/DD"
                  onChange={value => changeSelectedSearchDate(value)}
                  slotProps={{
                    textField: {
                      variant: 'outlined',
                      label: 'We are looking to name this'
                    }
                  }}
                />
              </LocalizationProvider>
            </Box>
            <FilterTable
              onSubmit={onSearch}
              onCancel={() => setSearchQuery([])}
            />
          </AccordionDetails>
        </Accordion>

        <Paper sx={{ p: 1 }}>
          <Typography p={1} variant="h6">
            Search result
          </Typography>
          <DataGrid
            sx={{
              '& .super-app-theme--searchable': {
                backgroundColor: '#c5e1a5',
                '&:hover': {
                  backgroundColor: '#a2cf6e'
                }
              },
              '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
                outline: 'none'
              },
              '& .super-app-theme--header': {
                backgroundColor: '#274263',
                color: 'white'
              }
            }}
            getRowId={({ uid }) => uid}
            columns={columns}
            rows={expandeGoldenRecordsQuery?.data || []}
            pageSizeOptions={[25, 50, 100]}
            onRowDoubleClick={params =>
              navigate({ to: `/record-details/${params.row.uid}` })
            }
            getRowClassName={params => `${getClassName(params.row)}`}
            onPaginationModelChange={model => handlePagination(model)}
            paginationMode="server"
            loading={expandeGoldenRecordsQuery.isLoading}
            rowCount={goldenIds.length}
          />
        </Paper>
      </Box>
    </Container>
  )
}

export default Records
