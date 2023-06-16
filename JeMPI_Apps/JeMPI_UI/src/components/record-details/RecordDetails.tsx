import {
  Button,
  ButtonGroup,
  Container,
  Divider,
  Paper,
  Stack,
  Typography
} from '@mui/material'
import { DataGrid, GridColDef } from '@mui/x-data-grid'
import { useMatch } from '@tanstack/react-location'
import { useMutation, useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import DataGridCellInput from 'components/patient/DataGridCellInput'
import { useAppConfig } from 'hooks/useAppConfig'
import { useSnackbar } from 'notistack'
import { useState } from 'react'
import ApiClient from 'services/ApiClient'
import { DisplayField, FieldChangeReq } from 'types/Fields'
import { PatientRecord, GoldenRecord } from 'types/PatientRecord'
import { formatDate } from 'utils/formatters'

export interface UpdatedFields {
  [fieldName: string]: { oldValue: unknown; newValue: unknown }
}

const AUDIT_TRAIL_COLUMNS: GridColDef[] = [
  {
    field: 'created_at',
    headerName: 'CreatedAt',
    valueFormatter: ({ value }) => formatDate(value),
    sortable: false,
    disableColumnMenu: true
  },
  {
    field: 'inserted_at',
    headerName: 'InsertedAt',
    sortable: false,
    disableColumnMenu: true
  },
  {
    field: 'interaction_id',
    headerName: 'InteractionID',
    sortable: false,
    disableColumnMenu: true
  },
  {
    field: 'golden_id',
    headerName: 'GoldenID',
    sortable: false,
    disableColumnMenu: true
  },

  {
    field: 'entry',
    headerName: 'Event',
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  }
]

const RecordDetails = () => {
  const {
    data: { uid }
  } = useMatch()
  const { enqueueSnackbar } = useSnackbar()
  const { getFieldsByGroup, availableFields } = useAppConfig()
  const [isEditMode, setIsEditMode] = useState(false)
  const [updatedFields, setUpdatedFields] = useState<UpdatedFields>({})
  const [patientRecord, setPatientRecord] = useState<
    PatientRecord | GoldenRecord | undefined
  >(undefined)
  const columns: GridColDef[] = getFieldsByGroup('demographics').map(
    ({ fieldName, fieldLabel, readOnly, isValid, formatValue }) => {
      return {
        field: fieldName,
        headerName: fieldLabel,
        flex: 1,
        valueFormatter: ({ value }) => formatValue(value),
        sortable: false,
        disableColumnMenu: true,
        editable: !readOnly && isEditMode,
        // a Callback used to validate the user's input
        preProcessEditCellProps: ({ props }) => {
          return {
            ...props,
            error: isValid(props.value)
          }
        },
        renderEditCell: props => <DataGridCellInput {...props} />
      }
    }
  )

  const { data, error, isLoading, isError } = useQuery<any, AxiosError>({
    queryKey: ['record-details', uid],
    queryFn: async () => {
      const recordId = uid as string
      return await ApiClient.getExpandedGoldenRecords([recordId], true)
    },
    refetchOnWindowFocus: false
  })

  const { data: auditTrail, isLoading: isAuditTrailLoading } = useQuery<
    any,
    AxiosError
  >({
    queryKey: ['audit-trail', uid],
    queryFn: async () => {
      return await ApiClient.getGoldenRecordAuditTrail(patientRecord?.uid || '')
    },
    enabled: !!patientRecord,
    refetchOnWindowFocus: false
  })

  const updateRecord = useMutation({
    mutationKey: ['golden-record', uid],
    mutationFn: async (req: FieldChangeReq) => {
      return await ApiClient.updatedGoldenRecord(uid as string, req)
    },
    onSuccess: () => {
      enqueueSnackbar(`Successfully saved patient records`, {
        variant: 'success'
      })
    },
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Could not save record changes`, {
        variant: 'error'
      })
      console.log(`Oops! Error persisting data: ${error.message}`)
    }
  })

  const onDataChange = (newRow: PatientRecord | GoldenRecord) => {
    const newlyUpdatedFields: UpdatedFields = availableFields.reduce(
      (acc: UpdatedFields, curr: DisplayField) => {
        if (data && data[curr.fieldName] !== newRow[curr.fieldName]) {
          acc[curr.fieldLabel] = {
            oldValue: data[curr.fieldName],
            newValue: newRow[curr.fieldName]
          }
        }
        return acc
      },
      {}
    )
    setUpdatedFields({ ...newlyUpdatedFields })
    setPatientRecord(newRow)
    return newRow
  }

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <ApiErrorMessage error={error} />
  }

  if (!data) {
    return <NotFound />
  }

  return (
    <Container
      maxWidth={false}
      sx={{ display: 'flex', flexDirection: 'column', gap: '20px' }}
    >
      <Paper sx={{ p: 1 }}>
        <Stack
          p={1}
          display={'flex'}
          flexDirection={'row'}
          justifyContent={'space-between'}
        >
          <Typography variant="h6">Demographics</Typography>
          <Stack display={'flex'} flexDirection={'row'}>
            <Button
              onClick={() => setIsEditMode(true)}
              disabled={isEditMode === true}
            >
              Edit
            </Button>
            <Button disabled>Save</Button>
            <Button>Cancel</Button>
          </Stack>
        </Stack>
        <DataGrid
          getRowId={({ uid }) => uid}
          columns={columns}
          onRowClick={params => {
            console.log(params.row)
            setPatientRecord(params.row)
          }}
          rows={data}
          autoHeight={true}
          hideFooter={true}
          // processRowUpdate={newRow => onChange(newRow)}
        />
      </Paper>
      <Paper>
        <DataGrid
          getRowId={({ created_at }) => created_at}
          columns={AUDIT_TRAIL_COLUMNS}
          rows={auditTrail || []}
          autoHeight={true}
          hideFooter={true}
          processRowUpdate={newRow => onDataChange(newRow)}
          loading={isAuditTrailLoading}
        />
      </Paper>
    </Container>
  )
}

export default RecordDetails
