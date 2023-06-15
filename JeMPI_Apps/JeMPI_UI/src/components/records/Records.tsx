import { Container, Link } from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import { useAppConfig } from 'hooks/useAppConfig'
import { AnyRecord, PatientRecord, ValueOf } from 'types/PatientRecord'
import { FilterTable } from './FilterTable'
import { SearchParameter } from 'types/SimpleSearch'
import { useState } from 'react'
import { isPatientCorresponding } from 'hooks/useSearch'
import useExpandedGoldenRecords from 'hooks/useExpandedGoldenRecords'

const Records = () => {
  const { getFieldsByGroup } = useAppConfig()
  const [searchQuery, setSearchQuery] = useState<Array<SearchParameter>>([])
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 10
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

  const {
    expandeGoldenRecordsQuery: { data, isLoading, isError, error }
  } = useExpandedGoldenRecords(
    paginationModel.page * paginationModel.pageSize,
    paginationModel.pageSize
  )

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <ApiErrorMessage error={error} />
  }

  if (!data) {
    return <NotFound />
  }

  const records = data.map(
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
        autoHeight={true}
        getRowClassName={params => `${getClassName(params.row)}`}
      />
    </Container>
  )
}

export default Records
