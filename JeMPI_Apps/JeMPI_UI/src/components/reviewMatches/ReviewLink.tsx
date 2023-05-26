import { SearchOutlined } from '@mui/icons-material'
import { Box, Container, Divider, Stack, Typography } from '@mui/material'
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
import DataGrid from './DataGrid'
import Dialog from './Dialog'
import SearchModal from './SearchModal'
import Stepper from './Stepper'

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

const ReviewLink = () => {
  const { payload } = useSearch<ReviewLinkParams>()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()

  const [openLinkRecordDialog, setOpenLinkRecordDialog] = useState(false)
  const [isSearchModalVisible, setIsSearchModalVisible] = useState(false)
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
    candidateGoldenRecords,
    matchDetails,
    error,
    isLoading,
    isError
  } = useLinkReview(payload, refineSearchQuery)

  const mutateNotification = useMutation({
    mutationFn: ApiClient.updateNotification,
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error updating notification: ${error.message}`, {
        variant: 'error'
      })
      setOpenLinkRecordDialog(false)
    }
  })

  const updateNotification = (state: NotificationState) => {
    mutateNotification.mutate({
      notificationId: payload?.notificationId ? payload.notificationId : '',
      state: state
    })
  }

  const newGoldenRecord = useMutation({
    mutationFn: ApiClient.newGoldenRecord,
    onSuccess: () => {
      updateNotification(NotificationState.Actioned)
    },
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error creating new golden record: ${error.message}`, {
        variant: 'error'
      })
      setIsNewGoldenRecordDialogOpen(false)
    }
  })

  const linkRecord = useMutation({
    mutationFn: ApiClient.linkRecord,
    onSuccess: () => {
      enqueueSnackbar('Golden record accepted and notification closed', {
        variant: 'success'
      })
      navigate({ to: '/notifications' })
    },
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error linking to golden record: ${error.message}`, {
        variant: 'error'
      })
      setOpenLinkRecordDialog(false)
    }
  })

  const leavePending = () => {
    mutateNotification.mutate(
      {
        notificationId: payload?.notificationId ? payload.notificationId : '',
        state: NotificationState.Pending
      },
      {
        onSuccess: () => {
          enqueueSnackbar(
            'Notification kept as pending. Golden Record remains linked',
            {
              variant: 'warning'
            }
          )
          navigate({ to: '/notifications' })
        }
      }
    )
  }

  const createGoldenRecord = (id: string) => {
    newGoldenRecord.mutate(
      {
        patientID: patientRecord ? patientRecord.uid : '',
        goldenID: goldenRecord ? goldenRecord.uid : '',
        newGoldenID: id
      },
      {
        onSuccess: data => {
          enqueueSnackbar('New record linked', {
            variant: 'success'
          })
          navigate({ to: `/golden-record/${data.goldenUID}` })
        }
      }
    )
  }

  const linkRecords = (id: string, status?: NotificationState) => {
    linkRecord.mutate(
      {
        patientID: patientRecord ? patientRecord.uid : '',
        goldenID: goldenRecord ? goldenRecord.uid : '',
        newGoldenID: id
      },
      {
        onSuccess: () => {
          updateNotification(status ?? NotificationState.Actioned)
          navigate({ to: '/notifications' })
        }
      }
    )
  }

  const handleCancel = () => {
    setIsNewGoldenRecordDialogOpen(false)
    setOpenLinkRecordDialog(false)
    setIsAcceptLinkDialogOpen(false)
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

  const handleOpenLinkedRecordDialog = (uid: string) => {
    const tableDataTemp = candidateGoldenRecords.filter(d => d.uid === uid)

    if (patientRecord) setTableData([patientRecord, ...tableDataTemp])

    setOpenLinkRecordDialog(true)
    setCandidateUID(uid)
    close()
  }

  return (
    <Container maxWidth={false}>
      <Stepper />
      <PageHeader
        title={'Review Linked Patient Record'}
        description="Review the patient record and possible matches in detail."
        buttons={[
          <Button
            startIcon={<SearchOutlined />}
            variant="outlined"
            size="large"
            onClick={() => setIsSearchModalVisible(true)}
          >
            Refine Search
          </Button>
        ]}
      />
      <Divider
        sx={{
          mb: 3
        }}
      />
      <Box
        sx={{
          mb: 3,
          borderRadius: '4px',
          boxShadow: '0px 0px 0px 1px #E0E0E0'
        }}
      >
        <Typography pl={1.5} variant="dgSubTitle">
          PATIENT LINKED TO GOLDEN RECORD
        </Typography>

        <DataGrid
          data={matchDetails.filter((r: AnyRecord) => {
            if (r.type === 'Golden' || r.type === 'Current') {
              return r
            }
          })}
          sx={{
            '.MuiDataGrid-columnSeparator': {
              display: 'none'
            },
            '& .MuiDataGrid-columnHeaders': {
              backgroundColor: '#274263',
              color: '#FFF',
              borderRadius: '0px'
            },
            borderRadius: '0px',
            p: 0
          }}
        />
      </Box>
      <Typography variant="dgSubTitle">OTHER GOLDEN RECORDS</Typography>
      <DataGrid
        data={candidateGoldenRecords || []}
        onLinkedRecordDialogOpen={handleOpenLinkedRecordDialog}
        sx={{
          '& .MuiDataGrid-columnHeaders': { display: 'none' },
          '& .MuiDataGrid-virtualScroller': { marginTop: '0!important' }
        }}
      />
      <Stack direction="row" sx={{ mt: 3 }} justifyContent={'space-between'}>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" onClick={() => leavePending()}>
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={() => setIsAcceptLinkDialogOpen(true)}
          >
            Close
          </Button>
        </Stack>
        <Button
          variant="outlined"
          onClick={() => setIsNewGoldenRecordDialogOpen(true)}
        >
          Create new golden record
        </Button>
      </Stack>
      <SearchModal
        isOpen={isSearchModalVisible}
        onClose={() => setIsSearchModalVisible(false)}
        onChange={setRefineSearchQuery}
      />
      <Dialog
        buttons={[
          <Button onClick={() => handleCancel()}>Cancel</Button>,
          <Button
            onClick={() => createGoldenRecord(canditateUID)}
            isLoading={newGoldenRecord.isLoading}
            autoFocus
          >
            Unlink and create new record
          </Button>
        ]}
        content={`Are you sure you want to unlink the patient record ${patientRecord?.uid} and golden record ${goldenRecord?.uid} and create a new record?`}
        title="Confirm Records Unlinking"
        onClose={handleCancel}
        onOpen={isNewGoldenRecordDialogOpen}
      />
      <Dialog
        buttons={[
          <Button onClick={() => handleCancel()}>Cancel</Button>,
          <Button
            onClick={() =>
              linkRecords(goldenRecord?.uid || '', NotificationState.Accepted)
            }
            isLoading={newGoldenRecord.isLoading}
            autoFocus
          >
            Confirm
          </Button>
        ]}
        content={
          <>
            <Typography>
              This will accept the currently linked Golden Record and close the
              notification
            </Typography>
            <Typography>
              No changes will be made to the golden record link
            </Typography>
          </>
        }
        title="Close and continue?"
        onClose={handleCancel}
        onOpen={isAcceptLinkDialogOpen}
      />
      <Dialog
        buttons={[
          <Button variant="outlined" onClick={() => handleCancel()}>
            Cancel
          </Button>,
          <Button onClick={() => linkRecords(canditateUID)}>Confirm</Button>
        ]}
        content={<DataGrid data={tableData} hideAction={true} />}
        title={
          <Typography color={'#0000CD'} fontSize={20} fontWeight={500}>
            Link these records?
          </Typography>
        }
        subTitle="This will link the following records: "
        onClose={handleCancel}
        onOpen={openLinkRecordDialog}
        maxWidth="lg"
        fullWidth={true}
      />
    </Container>
  )
}

export default ReviewLink
