import { Refresh, Search, SearchOutlined } from '@mui/icons-material'
import {
  Container,
  Divider,
  Paper,
  Slider,
  Stack,
  Typography
} from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useLinkReview } from 'hooks/useLinkReview'
import { useSnackbar } from 'notistack'
import { useState } from 'react'
import { CustomSearchQuery, SearchQuery } from 'types/SimpleSearch'
import {
  AnyRecord,
  GoldenRecord,
  PatientRecord
} from '../../types/PatientRecord'
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
import { useLocation, useNavigate } from 'react-router-dom'
import { useConfig } from 'hooks/useConfig'
import { NotificationRequest } from 'types/BackendResponse'

const getRowClassName = (type?: string) => {
  switch (type) {
    case 'Current':
      return 'super-app-theme--Current'
    default:
      return ''
  }
}

const ReviewLink = () => {
  const {
    state: { payload }
  } = useLocation()
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
  const { apiClient } = useConfig()

  const {
    patientRecord,
    goldenRecord,
    thresholdCandidates,
    candidateGoldenRecords,
    error,
    isLoading,
    isError
  } = useLinkReview(payload, refineSearchQuery, candidateThreshold)
  const { linkRecords, createNewGoldenRecord } = useRelink()

  const mutateNotification = useMutation({
    mutationFn: (request: NotificationRequest) =>
      apiClient.updateNotification(request),
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error updating notification: ${error.message}`, {
        variant: 'error'
      })
      setIsLinkRecordDialogOpen(false)
    }
  })

  const updateNotification = () => {
    mutateNotification.mutate(
      {
        notificationId: payload?.notificationId
      },
      {
        onSuccess: () => {
          navigate('/notifications')
        }
      }
    )
  }

  const createGoldenRecord = (id: string) => {
    createNewGoldenRecord.mutate(
      {
        patientID: payload?.patient_id || '',
        goldenID: goldenRecord ? goldenRecord.uid : '',
        newGoldenID: id
      },
      {
        onSuccess: data => {
          if (payload?.notificationId) {
            updateNotification()
          }
          enqueueSnackbar('New record linked', {
            variant: 'success'
          })
          navigate(`/record-details/${data.goldenUID}`)
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

  const linkToCandidateRecord = (id: string) => {
    goldenRecord &&
      linkRecords.mutate(
        {
          patientID: payload.patient_id,
          goldenID: goldenRecord.uid,
          newGoldenID: id
        },
        {
          onSuccess: () => {
            if (payload?.notificationId) {
              updateNotification()
              navigate('/notifications')
            } else {
              navigate(`/record-details/${id}`)
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

  if (!goldenRecord) {
    return <NotFound />
  }

  const matches = goldenRecord
    ? [
        ...goldenRecord.linkRecords.filter(
          record => record.uid !== patientRecord?.uid
        ),
        ...(patientRecord ? [patientRecord] : []),
        goldenRecord
      ]
    : []

  const handleOpenLinkedRecordDialog = (uid: string) => {
    const tableDataTemp: AnyRecord[] | undefined = payload?.notificationId
      ? candidateGoldenRecords?.filter(ele => ele.uid === uid)
      : thresholdCandidates?.filter(ele => ele.uid === uid)
    if (patientRecord && tableDataTemp)
      setTableData([patientRecord, ...tableDataTemp])

    setIsLinkRecordDialogOpen(true)
    setCandidateUID(uid)
  }

  const handleCancel = () => {
    navigate(`/record-details/${goldenRecord?.uid}`)
  }

  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Review Linked Patient Record'}
        breadcrumbs={[
          { icon: <Search />, title: 'Browse Records', link: '/browse-records' }
        ]}
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
      <Stack padding={'2rem 1rem 1rem 1rem'}>
        <Paper sx={{ mb: 2, mt: 3 }}>
          <Typography pl={1} variant="dgSubTitle">
            PATIENT LINKED TO GOLDEN RECORD
          </Typography>
          <CustomDataGrid<GoldenRecord | PatientRecord>
            rows={matches}
            sx={{
              borderRadius: '0px'
            }}
            getRowClassName={params =>
              params.row.uid === payload?.patient_id
                ? 'super-app-theme--SelectedPatient'
                : 'type' in params.row
                ? getRowClassName(params.row.type)
                : ''
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
          {!payload.notificationId && (
            <>
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
            </>
          )}
        </Stack>
        <Paper>
          <CustomDataGrid<AnyRecord>
            rows={
              payload?.notificationId
                ? candidateGoldenRecords || []
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
          onConfirm={() => linkToCandidateRecord(goldenRecord?.uid)}
        />
        <LinkRecordsDialog
          isOpen={isLinkRecordDialogOpen}
          data={tableData}
          onClose={handleModalCancel}
          onConfirm={() => linkToCandidateRecord(canditateUID)}
        />
      </Stack>
    </Container>
  )
}

export default ReviewLink
