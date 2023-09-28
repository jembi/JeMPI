import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useSnackbar } from 'notistack'
import { useMemo } from 'react'
import ApiClient from 'services/ApiClient'
import { AnyRecord, GoldenRecord } from 'types/PatientRecord'
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

export const useLinkReview = (
  payload: ReviewLinkParams | undefined,
  refineSearchQuery: SearchQuery | CustomSearchQuery | undefined,
  matchThreshold?: number
) => {
  const { enqueueSnackbar } = useSnackbar()

  const {
    data: matchDetails,
    error,
    isLoading,
    isError
  } = useQuery<[GoldenRecord, GoldenRecord[]], AxiosError>({
    queryKey: ['matchDetails', payload],
    queryFn: () => {
      return ApiClient.getMatchDetails(
        payload?.golden_id || '',
        payload?.candidates?.map(c => c.golden_id) || []
      )
    },
    refetchOnWindowFocus: false,
    enabled: !!payload
  })

  const [goldenRecord, blockedGoldenRecords] = matchDetails || [
    undefined,
    undefined
  ]

  const { data: refineSearchData } = useQuery<ApiSearchResult, AxiosError>({
    queryKey: ['search', refineSearchQuery],
    queryFn: () => {
      return ApiClient.searchQuery(
        refineSearchQuery ? refineSearchQuery : ({} as SearchQuery),
        true
      )
    },
    enabled: !!refineSearchQuery,
    onSuccess: () => {
      enqueueSnackbar(`Refined search results`, {
        variant: 'default'
      })
    },
    refetchOnWindowFocus: false
  })

  const searchedCandidates: GoldenRecord[] = useMemo(
    () =>
      ((refineSearchData?.records.data || []) as GoldenRecord[]).map(
        record => ({
          ...record,
          type: 'Searched'
        })
      ),
    [refineSearchData?.records.data]
  )

  const patientRecord = useMemo(() => {
    return goldenRecord?.linkRecords.find(
      (record: AnyRecord) => record.uid === payload?.patient_id
    )
  }, [goldenRecord?.linkRecords, payload?.patient_id])

  const thresholdCandidates = useQuery<AnyRecord[]>({
    queryKey: ['candidates', patientRecord?.uid, matchThreshold],
    queryFn: () =>
      ApiClient.getCandidates(
        patientRecord?.demographicData || {},
        matchThreshold || 0
      ),
    refetchOnWindowFocus: false,
    enabled: !!patientRecord
  })

  return {
    patientRecord,
    goldenRecord,
    candidateGoldenRecords: blockedGoldenRecords?.concat(searchedCandidates),
    thresholdCandidates: thresholdCandidates.data?.concat(searchedCandidates),
    matchDetails,
    error,
    isLoading,
    isError
  }
}
