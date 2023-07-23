import { Search } from '@mui/icons-material'
import {
  Box,
  Button,
  Container,
  Divider,
  Paper,
  Stack,
  Typography
} from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import { useMatch } from '@tanstack/react-location'
import { useMutation, useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import SourceIdComponent from 'components/browseRecords/SourceIdComponent'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import ConfirmationModal from 'components/patient/ConfirmationModal'
import DataGridCellInput from 'components/patient/DataGridCellInput'
import PageHeader from 'components/shell/PageHeader'
import { useAppConfig } from 'hooks/useAppConfig'
import { useSnackbar } from 'notistack'
import { useState } from 'react'
import ApiClient from 'services/ApiClient'
import { DisplayField, FieldChangeReq, FieldType } from 'types/Fields'
import { PatientRecord, GoldenRecord } from 'types/PatientRecord'
import { formatDate } from 'utils/formatters'
import { sortColumns } from 'utils/helpers'

export interface UpdatedFields {
  [fieldName: string]: { oldValue: unknown; newValue: unknown }
}

const AUDIT_TRAIL_COLUMNS: GridColDef[] = [
  {
    field: 'created_at',
    headerName: 'CreatedAt',
    valueFormatter: ({ value }) => formatDate(value),
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'inserted_at',
    headerName: 'InsertedAt',
    valueFormatter: ({ value }) => formatDate(value),
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'interaction_id',
    headerName: 'InteractionID',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'golden_id',
    headerName: 'GoldenID',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },

  {
    field: 'entry',
    headerName: 'Event',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header',
    flex: 1
  }
]

const RecordDetails = () => {
  const {
    data: { uid }
  } = useMatch()
  const { enqueueSnackbar } = useSnackbar()
  const { availableFields } = useAppConfig()
  const [isEditMode, setIsEditMode] = useState(false)
  const [updatedFields, setUpdatedFields] = useState<UpdatedFields>({})
  const [patientRecord, setPatientRecord] = useState<
    PatientRecord | GoldenRecord | undefined
  >(undefined)

  const [isModalVisible, setIsModalVisible] = useState(false)
  const columns: GridColDef[] = sortColumns(
    availableFields.map(
      ({ fieldName, fieldLabel, readOnly, isValid, formatValue }) => {
        return {
          field: fieldName,
          headerName: fieldLabel,
          flex: fieldName === 'sourceId' ? 2 : 1,
          valueFormatter: ({ value }) =>
            fieldName === 'createdAt'
              ? formatDate(value as Date)
              : formatValue(value),
          sortable: false,
          disableColumnMenu: true,
          editable: !readOnly && isEditMode && patientRecord?.type === 'Golden',
          // a Callback used to validate the user's input
          preProcessEditCellProps: ({ props }) => {
            return {
              ...props,
              error: isValid(props.value)
            }
          },
          renderEditCell: props => <DataGridCellInput {...props} />,
          headerClassName: 'super-app-theme--header',
          renderCell: (params: GridRenderCellParams) => {
            if (fieldName === 'sourceId') {
              return <SourceIdComponent content={params.row.sourceId} />
            }
          }
        }
      }
    ),
    [
      'uid',
      'createdAt',
      'sourceId',
      'auxId',
      'givenName',
      'familyName',
      'gender',
      'dob',
      'city',
      'phoneNumber',
      'nationalId',
      'score'
    ]
  )

  const { data, error, isLoading, isError } = useQuery<
    Array<GoldenRecord>,
    AxiosError
  >({
    queryKey: ['record-details', uid],
    queryFn: async () => {
      const recordId = uid as string
      return (await ApiClient.getExpandedGoldenRecords(
        [recordId],
        true
      )) as Array<GoldenRecord>
    },
    onSuccess: data => setPatientRecord(data[0]),
    refetchOnWindowFocus: false
  })

  const {
    data: auditTrail,
    isLoading: isAuditTrailLoading,
    isFetching
  } = useQuery<any, AxiosError>({
    queryKey: ['audit-trail', patientRecord?.uid],
    queryFn: async () => {
      if (patientRecord?.type === 'Golden') {
        return await ApiClient.getGoldenRecordAuditTrail(
          patientRecord?.uid || ''
        )
      } else {
        return await ApiClient.getInteractionAuditTrail(
          patientRecord?.uid || ''
        )
      }
    },
    enabled: !!patientRecord,
    refetchOnWindowFocus: false
  })

  const updateRecord = useMutation({
    mutationKey: ['golden-record', patientRecord?.uid],
    mutationFn: async (req: FieldChangeReq) => {
      return await ApiClient.updatedGoldenRecord(
        patientRecord?.uid as string,
        req
      )
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
        if (data && data[0][curr.fieldName] !== newRow[curr.fieldName]) {
          acc[curr.fieldLabel] = {
            oldValue: data[0][curr.fieldName],
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

  const onRecordSave = () => {
    setIsModalVisible(true)
  }

  const onConfirm = () => {
    if (patientRecord) {
      const fields = Object.keys(patientRecord).reduce(
        (
          acc: { name: string; oldValue: FieldType; newValue: FieldType }[],
          curr: string
        ) => {
          if (patientRecord && data[0][curr] !== patientRecord[curr]) {
            acc.push({
              name: curr,
              oldValue: data[0][curr] as FieldType,
              newValue: patientRecord[curr] as FieldType
            })
          }
          return acc
        },
        []
      )
      updateRecord.mutate({ fields })
    }

    setIsModalVisible(false)
    setIsEditMode(false)
    setUpdatedFields({})
  }
  const onCancelEditing = () => {
    setPatientRecord(data[0])
    setIsEditMode(false)
  }

  const onCancelConfirmation = () => {
    setIsModalVisible(false)
  }

  return (
    <Container
      maxWidth={false}
      sx={{
        '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
          outline: 'none'
        },
        '& .super-app-theme--header': {
          backgroundColor: '#274263',
          color: 'white'
        }
      }}
    >
      <PageHeader
        breadcrumbs={[{ icon: <Search />, title: 'Browse Records', link: '/' }]}
        title={`Patient interactions for GID ${uid}`}
      />
      <Divider />
      <ConfirmationModal
        isVisible={isModalVisible}
        handleClose={onCancelConfirmation}
        updatedFields={updatedFields}
        onConfirm={onConfirm}
      />
      <Box
        sx={{
          mt: '20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px'
        }}
      >
        <Paper sx={{ p: 1 }}>
          <Stack
            p={1}
            display={'flex'}
            flexDirection={'row'}
            justifyContent={'space-between'}
          >
            <Typography variant="h6">Records</Typography>
            <Stack display={'flex'} flexDirection={'row'}>
              <Button
                onClick={() => setIsEditMode(true)}
                disabled={
                  isEditMode === true || patientRecord?.type !== 'Golden'
                }
              >
                Edit
              </Button>
              <Button
                onClick={() => onRecordSave()}
                disabled={
                  isEditMode !== true || patientRecord?.type !== 'Golden'
                }
              >
                Save
              </Button>
              <Button
                disabled={
                  isEditMode !== true || patientRecord?.type !== 'Golden'
                }
                onClick={() => onCancelEditing()}
              >
                Cancel
              </Button>
            </Stack>
          </Stack>
          <DataGrid
            getRowId={({ uid }) => uid}
            columns={columns}
            onRowClick={params => {
              setPatientRecord(params.row)
            }}
            rows={data}
            autoHeight={true}
            hideFooter={true}
            sx={{
              '& .super-app-theme--Golden': {
                backgroundColor: '#f5df68',
                '&:hover': {
                  backgroundColor: '#fff08d'
                },
                '&.Mui-selected': {
                  backgroundColor: '#e2be1d',
                  '&:hover': { backgroundColor: '#fff08d' }
                }
              }
            }}
            getRowClassName={params => `super-app-theme--${params.row.type}`}
            processRowUpdate={newRow => onDataChange(newRow)}
          />
        </Paper>
        <Paper sx={{ p: 1 }}>
          <Typography p={1} variant="h6">
            Audit Trail
          </Typography>
          <DataGrid
            getRowId={({ created_at }) => created_at}
            columns={AUDIT_TRAIL_COLUMNS}
            rows={auditTrail || []}
            autoHeight={true}
            hideFooter={true}
            processRowUpdate={newRow => onDataChange(newRow)}
            loading={isAuditTrailLoading && isFetching}
          />
        </Paper>
      </Box>
    </Container>
  )
}

export default RecordDetails
