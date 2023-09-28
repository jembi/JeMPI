import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useSnackbar } from 'notistack'
import { useState } from 'react'
import ApiClient from 'services/ApiClient'
import { AnyRecord } from 'types/PatientRecord'
import {
  ApiSearchResult,
  CustomSearchQuery,
  SearchQuery
} from 'types/SimpleSearch'

interface ReviewLinkParams {
  notificationId: string
  patient_id: string
  golden_id: string
  score: number
  candidates: { golden_id: string; score: number }[]
}

const getRecordByType = (
  type: 'Golden' | 'Current' | 'Candidate',
  data: AnyRecord[]
) =>
  data.find((r: AnyRecord) => {
    if (r.type === type) {
      return r
    }
  })

const mapDataToScores = (
  data?: AnyRecord[],
  candidates?: { golden_id: string; score: number }[]
): AnyRecord[] => {
  if (!data?.length) {
    return []
  }
  return data.map(d => ({
    ...d,
    score: candidates?.find(c => c.golden_id === d.uid)?.score || 0
  }))
}

export const useLinkReview = (
  payload: ReviewLinkParams | undefined,
  refineSearchQuery: SearchQuery | CustomSearchQuery | undefined,
  matchThreshold?: number
) => {
  const { enqueueSnackbar } = useSnackbar()
  const [candidateGoldenRecords, setCandidateGoldenRecords] = useState<
    AnyRecord[]
  >([])
  const [goldenRecord, setGoldenRecord] = useState<AnyRecord | undefined>(
    undefined
  )
  const [patientRecord, setPatientRecord] = useState<AnyRecord | undefined>(
    undefined
  )

  const {
    data: matchDetails,
    error,
    isLoading,
    isError
  } = useQuery<AnyRecord[], AxiosError>({
    queryKey: ['matchDetails', payload],
    queryFn: () => {
      return ApiClient.getMatchDetails(
        payload?.patient_id || '',
        payload?.golden_id || '',
        payload?.candidates?.map(c => c.golden_id) || []
      )
    },
    onSuccess: data => {
      setCandidateGoldenRecords(
        mapDataToScores(
          data.filter((r: AnyRecord) => {
            if (r.type === 'Candidate') {
              return r
            }
          }),
          payload?.candidates
        )
      )
      setGoldenRecord(getRecordByType('Golden', data))
      setPatientRecord(getRecordByType('Current', data))
    },
    refetchOnWindowFocus: false
  })

  useQuery<ApiSearchResult, AxiosError>({
    queryKey: ['search', refineSearchQuery],
    queryFn: () => {
      return ApiClient.searchQuery(
        refineSearchQuery ? refineSearchQuery : ({} as SearchQuery),
        true
      )
    },
    enabled: !!refineSearchQuery,
    onSuccess: data => {
      const refineSearchResult = data.records.data.map(record => ({
        ...record,
        searched: true,
        type: 'Candidate'
      }))
      if (candidateGoldenRecords) {
        setCandidateGoldenRecords([
          ...mapDataToScores(
            matchDetails?.filter((r: AnyRecord) => {
              if (r.type === 'Candidate') {
                return r
              }
            }),
            payload?.candidates
          ),
          ...(refineSearchResult as unknown as AnyRecord[])
        ])
        enqueueSnackbar(`Refined search results`, {
          variant: 'default'
        })
      }
    },
    refetchOnWindowFocus: false
  })

  const thresholdCandidates = useQuery<AnyRecord[]>({
    queryKey: ['candidates', patientRecord?.uid, matchThreshold],
    queryFn: () =>
      ApiClient.getCandidates(
        {
          givenName: patientRecord?.givenName,
          familyName: patientRecord?.familyName,
          dob: patientRecord?.dob,
          gender: patientRecord?.gender,
          phoneNumber: patientRecord?.phoneNumber,
          city: patientRecord?.city,
          nationalId: patientRecord?.nationalId
        },
        matchThreshold || 0
      ),
    refetchOnWindowFocus: false
  })

  return {
    patientRecord,
    goldenRecord,
    candidateGoldenRecords,
    thresholdCandidates: thresholdCandidates.data,
    matchDetails,
    error,
    isLoading,
    isError
  }
}
