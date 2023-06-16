import { Box, Container, Link } from '@mui/material'
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

const Records = () => {
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
        align: 'center',
        headerAlign: 'center',
        filterable: false,
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

  const [idsPaginationModel, setIdsPaginationModel] = useState()
  const [displayedSize, setDisplayedSize] = useState(10)
  const [fetchedSize, setFetchedSize] = useState(100)

  const goldenIdsQuery = useQuery<Array<string>, AxiosError>({
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
      await ApiClient.getExpandedGoldenRecords(goldenIdsQuery?.data, false),
    enabled: !!goldenIdsQuery.data,
    refetchOnWindowFocus: false
  })

  if (expandeGoldenRecordsQuery.isError) {
    return <ApiErrorMessage error={expandeGoldenRecordsQuery.error} />
  }

  const records = expandeGoldenRecordsQuery?.data?.map(
    (record: {
      uid: string
      record: AnyRecord
      type: string
      score: number | null
    }) => ({
      ...record.record,
      uid: record.uid,
      type: record.type,
      score: record.score
    })
  )

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
      <FilterTable onSubmit={onSearch} onCancel={() => setSearchQuery([])} />
      <Box sx={{ position: 'static' }}>
        <DataGrid
          sx={{
            '& .super-app-theme--searchable': {
              backgroundColor: '#c5e1a5',
              '&:hover': {
                backgroundColor: '#a2cf6e'
              }
            }
          }}
          getRowId={({ uid }) => uid}
          columns={columns}
          rows={records || []}
          getRowClassName={params => `${getClassName(params.row)}`}
          onPaginationModelChange={setPaginationModel}
          paginationMode="server"
          loading={expandeGoldenRecordsQuery.isLoading}
          rowCount={1000}
        />
      </Box>
    </Container>
  )
}

export default Records
