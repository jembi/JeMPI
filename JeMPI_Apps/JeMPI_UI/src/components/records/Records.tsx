import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Container,
  Divider,
  Link,
  Paper,
  Typography
} from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import { useAppConfig } from 'hooks/useAppConfig'
import { AnyRecord, PatientRecord, ValueOf } from 'types/PatientRecord'
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

const Records = () => {
  const navigate = useNavigate()
  const { getFieldsByGroup } = useAppConfig()
  const [searchQuery, setSearchQuery] = useState<Array<SearchParameter>>([])
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 25
  })
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
        align:
          fieldName === 'givenName' ||
          fieldName === 'familyName' ||
          fieldName === 'city' ||
          fieldName === 'gender'
            ? 'left'
            : fieldName === 'dob'
            ? 'right'
            : 'center',
        headerAlign:
          fieldName === 'givenName' ||
          fieldName === 'familyName' ||
          fieldName === 'city' ||
          fieldName === 'gender'
            ? 'left'
            : fieldName === 'dob'
            ? 'right'
            : 'center',
        filterable: false,
        headerClassName: 'super-app-theme--header',
        renderCell: (params: GridRenderCellParams) => {
          if (fieldName === 'uid') {
            return (
              <Link
                href={`/patient-record/${params.row.uid}`}
                key={params.row.uid}
              >
                {params.row.uid}
              </Link>
            )
          }
        }
      }
    }
  )

  const goldenIdsQuery = useQuery<any, AxiosError>({
    queryKey: ['golden-records-ids', { ...paginationModel }],
    queryFn: async () =>
      await ApiClient.getGoldenIds(
        paginationModel.page * paginationModel.pageSize,
        paginationModel.pageSize
      ),
    enabled: true,
    refetchOnWindowFocus: false
  })

  const expandeGoldenRecordsQuery = useQuery<any, AxiosError>({
    queryKey: ['expanded-golden-records', { ...paginationModel }],
    queryFn: async () =>
      await ApiClient.getExpandedGoldenRecords(
        goldenIdsQuery?.data.goldenIds,
        false
      ),
    enabled: !!goldenIdsQuery.data,
    refetchOnWindowFocus: false
  })

  if (expandeGoldenRecordsQuery.isError) {
    return <ApiErrorMessage error={expandeGoldenRecordsQuery.error} />
  }

  const onSearch = (query: SearchParameter[]) => {
    setSearchQuery(query)
  }

  const getClassName = (patient: PatientRecord) => {
    return isPatientCorresponding(patient, searchQuery)
      ? `super-app-theme--searchable`
      : ''
  }

  return (
    <Container
      maxWidth={false}
      sx={{ display: 'flex', flexDirection: 'column', gap: '20px' }}
    >
      <PageHeader title={`Browse Patients`} />
      <Divider />
      <Accordion>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          aria-controls="panel1a-content"
          id="panel1a-header"
        >
          <Typography variant="h6">Filter by</Typography>
        </AccordionSummary>
        <AccordionDetails></AccordionDetails>
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
          onPaginationModelChange={setPaginationModel}
          paginationMode="server"
          loading={expandeGoldenRecordsQuery.isLoading}
          rowCount={goldenIdsQuery?.data?.pagination?.total}
        />
      </Paper>
    </Container>
  )
}

export default Records
