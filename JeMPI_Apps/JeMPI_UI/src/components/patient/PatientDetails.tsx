import { Person } from '@mui/icons-material'
import SearchIcon from '@mui/icons-material/Search'
import { Box, ButtonGroup, Container, Grid } from '@mui/material'
import { useMatch } from '@tanstack/react-location'
import { useMutation, useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useSnackbar } from 'notistack'
import { FC, useEffect, useState } from 'react'
import { useAppConfig } from '../../hooks/useAppConfig'
import ApiClient from '../../services/ApiClient'
import { DisplayField, FieldChangeReq, FieldType } from '../../types/Fields'
import { GoldenRecord, PatientRecord } from '../../types/PatientRecord'
import Loading from '../common/Loading'
import ApiErrorMessage from '../error/ApiErrorMessage'
import NotFound from '../error/NotFound'
import Button from '../shared/Button'
import PageHeader from '../shell/PageHeader'
import AddressPanel from './AddressPanel'
import ConfirmationModal from './ConfirmationModal'
import DemographicsPanel from './DemographicsPanel'
import IdentifiersPanel from './IdentifiersPanel'
import RegisteringFacilityPanel from './RegisteringFacilityPanel'
import RelationshipPanel from './RelationshipPanel'
import SubHeading from './SubHeading'

export interface UpdatedFields {
  [fieldName: string]: { oldValue: unknown; newValue: unknown }
}

type PatientDetailsProps = {
  isGoldenRecord: boolean
}

const PatientDetails: FC<PatientDetailsProps> = ({ isGoldenRecord }) => {
  const {
    data: { uid }
  } = useMatch()
  const { availableFields } = useAppConfig()
  const { enqueueSnackbar } = useSnackbar()
  const { getPatientName } = useAppConfig()
  const [patientRecord, setPatientRecord] = useState<
    PatientRecord | GoldenRecord | undefined
  >(undefined)
  const [isEditMode, setIsEditMode] = useState(false)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [updatedFields, setUpdatedFields] = useState<UpdatedFields>({})
  const { data, error, isLoading, isError } = useQuery<
    PatientRecord | GoldenRecord,
    AxiosError
  >({
    queryKey: [isGoldenRecord ? 'golden-record' : 'patient-record', uid],
    queryFn: async () => {
      if (isGoldenRecord) {
        return await ApiClient.getGoldenRecord(uid as string)
      } else {
        return await ApiClient.getPatientRecord(uid as string)
      }
    },
    refetchOnWindowFocus: false
  })

  const updatePatientRecord = useMutation({
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

  console.log(updatedFields)
  const isEditable = isGoldenRecord && isEditMode

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

  const onDataSave = () => {
    setIsModalVisible(true)
  }

  useEffect(() => {
    if (patientRecord === undefined) {
      setPatientRecord(data)
    }
  }, [data, patientRecord])

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <ApiErrorMessage error={error} />
  }

  if (!data || !patientRecord) {
    return <NotFound />
  }

  const onConfirm = () => {
    const fields = Object.keys(patientRecord).reduce(
      (acc: { name: string; value: FieldType }[], curr: string) => {
        if (patientRecord && data[curr] !== patientRecord[curr]) {
          acc.push({ name: curr, value: patientRecord[curr] as FieldType })
        }
        return acc
      },
      []
    )
    updatePatientRecord.mutate({ fields })
    setIsModalVisible(false)
    setIsEditMode(false)
    setUpdatedFields({})
  }
  const onCancelEditing = () => {
    setPatientRecord(data)
    setIsEditMode(false)
  }

  const onCancelConfirmation = () => {
    setIsModalVisible(false)
  }

  const patientName = getPatientName(data)

  return (
    <Container maxWidth={false}>
      <ConfirmationModal
        isVisible={isModalVisible}
        handleClose={onCancelConfirmation}
        updatedFields={updatedFields}
        onConfirm={onConfirm}
      />
      <PageHeader
        description={<SubHeading data={data} isGoldenRecord={isGoldenRecord} />}
        title={patientName}
        color={isGoldenRecord ? '#FBC02D' : '#01579B'}
        breadcrumbs={[
          {
            icon: <SearchIcon />,
            title: 'Search Results'
          },
          {
            icon: <Person />,
            title: `${isGoldenRecord ? 'Golden' : 'Patient'} Record Details`
          }
        ]}
        buttons={
          isGoldenRecord
            ? [
                <Button
                  variant="outlined"
                  href={`/patient/${uid}/audit-trail`}
                  key="audit-trail"
                >
                  AUDIT TRAIL
                </Button>,

                <Button
                  variant="header"
                  href={`/golden-record/${uid}/linked-records`}
                  key="linked-records"
                >
                  LINKED RECORDS
                </Button>
              ]
            : []
        }
      />
      <Grid container spacing={4}>
        <Grid item xs={4}>
          <IdentifiersPanel
            data={patientRecord}
            isEditable={isEditable}
            onChange={onDataChange}
          />
        </Grid>
        <Grid item xs={3}>
          <RegisteringFacilityPanel
            data={
              Array.isArray(patientRecord.sourceId)
                ? patientRecord.sourceId
                : [patientRecord.sourceId]
            }
          />
        </Grid>
        <Grid item xs={5}>
          <AddressPanel
            data={patientRecord}
            isEditable={isEditable}
            onChange={onDataChange}
          />
        </Grid>
        <Grid item xs={8}>
          <DemographicsPanel
            data={patientRecord}
            isEditable={isEditable}
            onChange={onDataChange}
          />
        </Grid>
        <Grid item xs={4}>
          <RelationshipPanel
            data={patientRecord}
            isEditable={isEditable}
            onChange={onDataChange}
          />
        </Grid>
      </Grid>
      {isGoldenRecord && (
        <Box
          sx={{
            py: 4,
            display: 'flex',
            gap: '4px'
          }}
        >
          {isEditMode ? (
            <ButtonGroup>
              <Button onClick={() => onCancelEditing()} variant="outlined">
                Cancel
              </Button>
              <Button onClick={() => onDataSave()} variant="outlined">
                Save
              </Button>
            </ButtonGroup>
          ) : (
            <Button onClick={() => setIsEditMode(true)} variant="outlined">
              Edit Golden Record
            </Button>
          )}
        </Box>
      )}
    </Container>
  )
}

export default PatientDetails
