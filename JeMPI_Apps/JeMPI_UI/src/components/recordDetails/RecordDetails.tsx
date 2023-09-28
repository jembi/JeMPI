import { Search } from '@mui/icons-material'
import {
  Button,
  Container,
  Divider,
  Paper,
  Stack,
  Typography
} from '@mui/material'
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  GridRowSelectionModel
} from '@mui/x-data-grid'
import { useMatch, useNavigate } from '@tanstack/react-location'
import { useMutation, useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'
import ApiErrorMessage from 'components/error/ApiErrorMessage'
import NotFound from 'components/error/NotFound'
import ConfirmEditingDialog from 'components/recordDetails/ConfirmEditingDialog'
import DataGridCellInput from './DataGridCellInput'
import PageHeader from 'components/shell/PageHeader'
import { useAppConfig } from 'hooks/useAppConfig'
import { useSnackbar } from 'notistack'
import { useState } from 'react'
import ApiClient from 'services/ApiClient'
import { DisplayField, FieldChangeReq, FieldType } from 'types/Fields'
import { PatientRecord, GoldenRecord } from 'types/PatientRecord'
import { sortColumns } from 'utils/helpers'
import getCellComponent from 'components/shared/getCellComponent'
import { AUDIT_TRAIL_COLUMNS } from 'utils/constants'
import { AuditTrail } from 'types/AuditTrail'

export interface UpdatedFields {
  [fieldName: string]: { oldValue: unknown; newValue: unknown }
}

const RecordDetails = () => {
  const {
    data: { uid }
  } = useMatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { availableFields } = useAppConfig()
  const [isEditMode, setIsEditMode] = useState(false)
  const [updatedFields, setUpdatedFields] = useState<UpdatedFields>({})
  const [records, setRecords] = useState<Array<GoldenRecord | PatientRecord>>(
    []
  )
  const [patientRecord, setPatientRecord] = useState<
    PatientRecord | GoldenRecord | undefined
  >(undefined)
  const [rowSelectionModel, setRowSelectionModel] =
    useState<GridRowSelectionModel>([])

  const [isModalVisible, setIsModalVisible] = useState(false)
  const columns: GridColDef[] = availableFields.map(
    ({
      fieldName,
      fieldLabel,
      readOnly,
      validation,
      isValid,
      formatValue
    }) => ({
      field: fieldName,
      headerName: fieldLabel,
      flex: fieldName === 'sourceId' ? 2 : 1,
      valueFormatter: ({ value }) => formatValue(value),
      sortable: false,
      disableColumnMenu: true,
      editable: !readOnly && isEditMode && patientRecord?.type === 'Golden',
      // a Callback used to validate the user's input
      preProcessEditCellProps: ({ props }) => ({
        ...props,
        error: isValid(props.value)
      }),
      renderEditCell: props => (
        <DataGridCellInput
          {...{ ...props, message: validation?.onErrorMessage }}
        />
      ),
      headerClassName: 'super-app-theme--header',
      renderCell: (params: GridRenderCellParams) =>
        getCellComponent(fieldName, params)
    })
  )

  const sortedColumns = sortColumns(columns, [
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
  ])

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
    onSuccess: data => {
      setPatientRecord(data[0])
      setRowSelectionModel([data[0].uid])
      setRecords(data)
    },
    refetchOnWindowFocus: false
  })

  const {
    data: auditTrail,
    isLoading: isAuditTrailLoading,
    isFetching
  } = useQuery<Array<AuditTrail>, AxiosError>({
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
    onError: () => {
      enqueueSnackbar(`Could not save record changes`, {
        variant: 'error'
      })
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

  const handlePatientSelection = (record: PatientRecord) => {
    if (isEditMode && rowSelectionModel[0] !== record.uid) {
      enqueueSnackbar(
        'Please finish editing the golden record by saving it or by canceling your changes',
        {
          variant: 'warning'
        }
      )
    } else {
      setPatientRecord(record)
    }
  }
  const onCancelEditing = () => {
    setRecords([...data])
    setIsEditMode(false)
  }

  return (
    <Container
      maxWidth={false}
      sx={{
        '& .MuiDataGrid-cell:focus-within, & .MuiDataGrid-cell:focus': {
          outline: 'none'
        }
      }}
    >
      <PageHeader
        breadcrumbs={[
          { icon: <Search />, title: 'Browse Records', link: '/browse-records' }
        ]}
        title={`Patient interactions for GID ${uid}`}
      />
      <Divider />
      <ConfirmEditingDialog
        isVisible={isModalVisible}
        handleClose={() => setIsModalVisible(false)}
        updatedFields={updatedFields}
        onConfirm={onConfirm}
      />
      <Stack mt="20px" direction="column" gap="20px">
        <Paper sx={{ p: 1 }}>
          <Stack p={1} flexDirection={'row'} justifyContent={'space-between'}>
            <Typography variant="h6">Records</Typography>
            <Stack display={'flex'} flexDirection={'row'}>
              {!patientRecord || patientRecord?.type === 'Golden' ? (
                <Button
                  onClick={() => setIsEditMode(true)}
                  disabled={isEditMode === true}
                >
                  Edit
                </Button>
              ) : (
                <Button
                  onClick={() =>
                    navigate({
                      fromCurrent: true,
                      to: 'relink',
                      search: {
                        payload: {
                          patient_id: patientRecord.uid,
                          golden_id: data[0].uid,
                          score: patientRecord.score
                        }
                      }
                    })
                  }
                >
                  Relink
                </Button>
              )}
              <Button
                onClick={() => setIsModalVisible(true)}
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
            columns={sortedColumns}
            onRowClick={params => handlePatientSelection(params.row)}
            onRowSelectionModelChange={row => setRowSelectionModel(row)}
            rowSelectionModel={rowSelectionModel}
            disableRowSelectionOnClick={isEditMode}
            editMode="row"
            isCellEditable={params => params.row.type === 'Golden'}
            rows={records}
            autoHeight={true}
            hideFooter={true}
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
            loading={isAuditTrailLoading && isFetching}
          />
        </Paper>
      </Stack>
    </Container>
  )
}

export default RecordDetails
