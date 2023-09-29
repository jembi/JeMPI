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
  GridRowSelectionModel,
  GridValueSetterParams
} from '@mui/x-data-grid'
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
import { PatientRecord, GoldenRecord, AnyRecord } from 'types/PatientRecord'
import { sortColumns } from 'utils/helpers'
import getCellComponent from 'components/shared/getCellComponent'
import { AUDIT_TRAIL_COLUMNS } from 'utils/constants'
import { AuditTrail } from 'types/AuditTrail'
import { useLoaderData, useNavigate } from 'react-router-dom'

export interface UpdatedFields {
  [fieldName: string]: { oldValue: unknown; newValue: unknown }
}

const RecordDetails = () => {
  const uid = useLoaderData()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { availableFields } = useAppConfig()
  const [isEditMode, setIsEditMode] = useState(false)
  const [updatedFields, setUpdatedFields] = useState<UpdatedFields>({})
  const [records, setRecords] = useState<Array<GoldenRecord | PatientRecord>>(
    []
  )
  const [record, setPatientRecord] = useState<
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
      formatValue,
      getValue
    }) => ({
      field: fieldName,
      headerName: fieldLabel,
      flex: fieldName === 'sourceId' ? 2 : 1,
      valueFormatter: ({ value }) => formatValue(value),
      sortable: false,
      disableColumnMenu: true,
      editable: !readOnly && isEditMode && record && 'linkRecords' in record,
      // a Callback used to validate the user's input
      preProcessEditCellProps: ({ props }) => ({
        ...props,
        error: isValid(props.value)
      }),
      valueGetter: getValue,
      valueSetter: (params: GridValueSetterParams) => {
        return {
          ...params.row,
          demographicData: {
            ...params.row.demographicData,
            [fieldName]: params.value
          }
        }
      },
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
    Array<AnyRecord>,
    AxiosError
  >({
    queryKey: ['record-details', uid],
    queryFn: async () => {
      const recordId = uid as string
      return await ApiClient.getFlatExpandedGoldenRecords([recordId])
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
    queryKey: ['audit-trail', record?.uid],
    queryFn: async () => {
      if (record) {
        if ('linkRecords' in record) {
          return await ApiClient.getGoldenRecordAuditTrail(record.uid || '')
        } else {
          return await ApiClient.getInteractionAuditTrail(record.uid || '')
        }
      }
      throw new Error('Empty record')
    },
    enabled: !!record,
    refetchOnWindowFocus: false
  })

  const updateRecord = useMutation({
    mutationKey: ['golden-record', record?.uid],
    mutationFn: async (req: FieldChangeReq) => {
      return await ApiClient.updatedGoldenRecord(record?.uid as string, req)
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
        if (
          data &&
          data[0].demographicData[curr.fieldName] !==
            newRow.demographicData[curr.fieldName]
        ) {
          acc[curr.fieldLabel] = {
            oldValue: data[0].demographicData[curr.fieldName],
            newValue: newRow.demographicData[curr.fieldName]
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
    if (record) {
      const fields = Object.keys(record.demographicData).reduce(
        (
          acc: { name: string; oldValue: FieldType; newValue: FieldType }[],
          curr: string
        ) => {
          if (
            record &&
            data[0].demographicData[curr] !== record.demographicData[curr]
          ) {
            acc.push({
              name: curr,
              oldValue: data[0].demographicData[curr] as FieldType,
              newValue: record.demographicData[curr] as FieldType
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
              {!record || 'linkRecords' in record ? (
                <>
                  <Button
                    onClick={() => setIsEditMode(true)}
                    disabled={isEditMode === true}
                  >
                    Edit
                  </Button>

                  <Button
                    onClick={() => setIsModalVisible(true)}
                    disabled={!isEditMode}
                  >
                    Save
                  </Button>
                  <Button
                    disabled={!isEditMode}
                    onClick={() => onCancelEditing()}
                  >
                    Cancel
                  </Button>
                </>
              ) : (
                <Button
                  onClick={() =>
                    navigate(`/record-details/${data[0].uid}/relink`, {
                      state: {
                        payload: {
                          patient_id: record.uid,
                          golden_id: data[0].uid,
                          score: record.score
                        }
                      }
                    })
                  }
                >
                  Relink
                </Button>
              )}
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
            isCellEditable={params => params.row.type === 'Current'}
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
