import { Refresh, SearchOutlined } from '@mui/icons-material'
import {
  Container,
  Divider,
  Paper,
  Slider,
  Stack,
  Typography
} from '@mui/material'
import { MakeGenerics, useNavigate, useSearch } from '@tanstack/react-location'
import { useMutation } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useLinkReview } from 'hooks/useLinkReview'
import { useSnackbar } from 'notistack'
import { useState } from 'react'
import { CustomSearchQuery, SearchQuery } from 'types/SimpleSearch'
import ApiClient from '../../services/ApiClient'
import { NotificationState } from '../../types/Notification'
import { AnyRecord } from '../../types/PatientRecord'
import Loading from '../common/Loading'
import ApiErrorMessage from '../error/ApiErrorMessage'
import NotFound from '../error/NotFound'
import Button from '../shared/Button'
import PageHeader from '../shell/PageHeader'
import SearchDialog from './SearchDialog'
import CustomDataGrid from './CustomDataGrid'
import useRelink from 'hooks/useRelink'
import LinkRecordsDialog from './LinkRecordsDialog'
import CloseNotificationDialog from './CloseNotificationDialog'
import UnlinkingDialog from './UnlinkingDialog'

export type ReviewLinkParams = MakeGenerics<{
  Search: {
    payload: {
      notificationId: string
      patient_id: string
      golden_id: string
      score: number
      candidates: { golden_id: string; score: number }[]
    }
  }
}>

const getRowClassName = (type: string) => {
  switch (type) {
    case 'Golden':
      return 'super-app-theme--Golden'
    default:
      return ''
  }
}

const ReviewLink = () => {
  const { payload } = useSearch<ReviewLinkParams>()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()

  const [candidateThreshold, setCandidateThreshold] = useState(0)
  const [sliderThreshold, setSliderThreshold] = useState(0)
  const [isLinkRecordDialogOpen, setIsLinkRecordDialogOpen] = useState(false)
  const [isSearchDialogVisible, setIsSearchDialogVisible] = useState(false)
  const [isNewGoldenRecordDialogOpen, setIsNewGoldenRecordDialogOpen] =
    useState(false)
  const [isAcceptLinkDialogOpen, setIsAcceptLinkDialogOpen] = useState(false)
  const [tableData, setTableData] = useState<AnyRecord[]>([])
  const [canditateUID, setCandidateUID] = useState('')
  const [refineSearchQuery, setRefineSearchQuery] = useState<
    SearchQuery | CustomSearchQuery | undefined
  >(undefined)

  const {
    goldenRecord,
    patientRecord,
    thresholdCandidates,
    candidateGoldenRecords,
    matchDetails,
    error,
    isLoading,
    isError
  } = useLinkReview(payload, refineSearchQuery, candidateThreshold)
  const { linkRecords, createNewGoldenRecord } = useRelink()

  const mutateNotification = useMutation({
    mutationFn: ApiClient.updateNotification,
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error updating notification: ${error.message}`, {
        variant: 'error'
      })
      setIsLinkRecordDialogOpen(false)
    }
  })

  const updateNotification = (state: NotificationState) => {
    mutateNotification.mutate(
      {
        notificationId: payload?.notificationId ? payload.notificationId : '',
        state: state
      },
      {
        onSuccess: () => {
          if (state === NotificationState.Pending) {
            enqueueSnackbar(
              'Notification kept as pending. Golden Record remains linked',
              {
                variant: 'warning'
              }
            )
            navigate({ to: '/notifications' })
          }
        }
      }
    )
  }

  const createGoldenRecord = (id: string) => {
    createNewGoldenRecord.mutate(
      {
        patientID: patientRecord ? patientRecord.uid : '',
        goldenID: goldenRecord ? goldenRecord.uid : '',
        newGoldenID: id
      },
      {
        onSuccess: data => {
          if (payload?.notificationId) {
            updateNotification(NotificationState.Actioned)
          }
          enqueueSnackbar('New record linked', {
            variant: 'success'
          })
          navigate({ to: `/record-details/${data.goldenUID}` })
        },
        onError: (error: AxiosError) => {
          enqueueSnackbar(
            `Error creating new golden record: ${error.message}`,
            {
              variant: 'error'
            }
          )
          setIsNewGoldenRecordDialogOpen(false)
        }
      }
    )
  }

  const linkToCandidateRecord = (id: string, status?: NotificationState) => {
    linkRecords.mutate(
      {
        patientID: patientRecord ? patientRecord.uid : '',
        goldenID: goldenRecord ? goldenRecord.uid : '',
        newGoldenID: id
      },
      {
        onSuccess: () => {
          if (payload?.notificationId) {
            updateNotification(status ?? NotificationState.Actioned)
            navigate({ to: '/notifications' })
          } else {
            navigate({ to: `/record-details/${id}` })
          }
        }
      }
    )
  }

  const handleModalCancel = () => {
    setIsNewGoldenRecordDialogOpen(false)
    setIsLinkRecordDialogOpen(false)
    setIsAcceptLinkDialogOpen(false)
  }

  const handleThresholdChange = (_e: Event, value: number | number[]) => {
    if (!Array.isArray(value)) {
      setSliderThreshold(value)
    }
  }

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <ApiErrorMessage error={error} />
  }

  if (!matchDetails) {
    return <NotFound />
  }

  const matches = matchDetails
    .filter(record => record.type === 'Golden' || record.type === 'Current')
    .reduce(
      (acc: Array<AnyRecord>, curr: AnyRecord) =>
        curr.uid === payload?.patient_id || curr.type === 'Golden'
          ? [...acc, curr]
          : [curr, ...acc],
      []
    )

  const handleOpenLinkedRecordDialog = (uid: string) => {
    const tableDataTemp = candidateGoldenRecords.filter(d => d.uid === uid)

    if (patientRecord) setTableData([patientRecord, ...tableDataTemp])

    setIsLinkRecordDialogOpen(true)
    setCandidateUID(uid)
  }

  const handleCancel = () => {
    if (payload?.notificationId) {
      updateNotification(NotificationState.Pending)
    } else {
      navigate({ to: `/browse-records/record-details/${goldenRecord?.uid}` })
    }
  }

  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Review Linked Patient Record'}
        description="Review the patient record and possible matches in detail."
        buttons={[
          <Button
            startIcon={<SearchOutlined />}
            variant="outlined"
            size="large"
            onClick={() => setIsSearchDialogVisible(true)}
          >
            Refine Search
          </Button>
        ]}
      />
      <Divider />
      <Paper sx={{ mb: 2, mt: 3 }}>
        <Typography pl={1} variant="dgSubTitle">
          PATIENT LINKED TO GOLDEN RECORD
        </Typography>
        <CustomDataGrid
          rows={matches}
          sx={{
            borderRadius: '0px'
          }}
          getRowClassName={params =>
            params.row.uid === payload?.patient_id
              ? 'super-app-theme--SelectedPatient'
              : getRowClassName(params.row.type)
          }
        />
      </Paper>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        spacing={1}
      >
        <Typography flex={1} variant="dgSubTitle">
          OTHER GOLDEN RECORDS
        </Typography>
        {sliderThreshold !== candidateThreshold && (
          <Button
            sx={{ p: 0, minWidth: 0 }}
            onClick={() => setCandidateThreshold(sliderThreshold)}
          >
            <Refresh />
          </Button>
        )}
        <Stack
          direction="row"
          sx={{ width: 250 }}
          justifyContent="space-between"
          alignItems="center"
          spacing={2}
        >
          <Typography variant="dgSubTitle">THRESHOLD: </Typography>
          <Slider
            valueLabelDisplay="auto"
            step={0.05}
            size="small"
            min={0}
            max={1}
            value={sliderThreshold}
            onChange={handleThresholdChange}
          />
        </Stack>
      </Stack>
      <Paper>
        <CustomDataGrid
          rows={
            payload?.notificationId
              ? candidateGoldenRecords
              : thresholdCandidates?.filter(
                  record => record.uid !== payload?.golden_id
                ) || []
          }
          action={handleOpenLinkedRecordDialog}
          sx={{
            '& .MuiDataGrid-columnHeaders': { display: 'none' },
            '& .MuiDataGrid-virtualScroller': { marginTop: '0!important' }
          }}
        />
      </Paper>
      <Stack direction="row" sx={{ mt: 3 }} justifyContent={'space-between'}>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" onClick={handleCancel}>
            Cancel
          </Button>
          {payload?.notificationId && (
            <Button
              variant="contained"
              onClick={() => setIsAcceptLinkDialogOpen(true)}
            >
              Close
            </Button>
          )}
        </Stack>
        <Button
          variant="outlined"
          onClick={() => setIsNewGoldenRecordDialogOpen(true)}
        >
          Create new golden record
        </Button>
      </Stack>
      <SearchDialog
        isOpen={isSearchDialogVisible}
        onClose={() => setIsSearchDialogVisible(false)}
        onChange={setRefineSearchQuery}
      />
      <UnlinkingDialog
        isOpen={isNewGoldenRecordDialogOpen}
        onClose={() => handleModalCancel()}
        onConfirm={() => createGoldenRecord(canditateUID)}
      />
      <CloseNotificationDialog
        isOpen={isAcceptLinkDialogOpen}
        onClose={handleModalCancel}
        onConfirm={() =>
          linkToCandidateRecord(
            goldenRecord?.uid || '',
            NotificationState.Accepted
          )
        }
      />
      <LinkRecordsDialog
        isOpen={isLinkRecordDialogOpen}
        data={tableData}
        onClose={handleModalCancel}
        onConfirm={handleCancel}
      />
    </Container>
  )
}

export default ReviewLink
