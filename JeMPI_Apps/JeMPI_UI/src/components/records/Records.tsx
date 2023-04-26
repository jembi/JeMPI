import { Link } from '@mui/material'
import { DataGrid, GridColumns, GridRenderCellParams } from '@mui/x-data-grid'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import { useAppConfig } from 'hooks/useAppConfig'
import ApiClient from 'services/ApiClient'
import { AnyRecord } from 'types/PatientRecord'

const Records = () => {
  const { getFieldsByGroup } = useAppConfig()
  const columns: GridColumns = getFieldsByGroup('linked_records').map(
    ({ fieldName, fieldLabel, formatValue }) => {
      return {
        field: fieldName,
        headerName: fieldLabel,
        flex: 1,
        valueFormatter: ({ value }) => formatValue(value),
        sortable: false,
        disableColumnMenu: true,
        align: 'center',
        headerAlign: 'center',
        filterable: false,
        renderCell: (params: GridRenderCellParams<string>) => {
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

  const { data, isLoading, isError, error } = useQuery<any, AxiosError>({
    queryKey: ['golden-records'],
    queryFn: async () => await ApiClient.getExpandedGoldenRecords(),
    refetchOnWindowFocus: false
  })

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

  return (
    <>
      <DataGrid
        sx={{
          '& .super-app-theme--golden': {
            backgroundColor: '#FFC400'
          }
        }}
        getRowId={({ uid }) => uid}
        columns={columns}
        rows={records || []}
        autoHeight={true}
        getRowClassName={params => `super-app-theme--${params.row.type}`}
      />
    </>
  )
}

export default Records
