import { useMatch } from '@tanstack/react-location'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import ApiClient from '../services/ApiClient'
import AuditTrailRecord from '../types/AuditTrail'
import { GoldenRecord } from '../types/PatientRecord'

export const useAuditTrailQuery = () => {
  const {
    data: { uid }
  } = useMatch()
  const patientQuery = useQuery<GoldenRecord, AxiosError>({
    queryKey: ['golden-record', uid],
    queryFn: async () => await ApiClient.getGoldenRecord(uid as string),
    refetchOnWindowFocus: false
  })
  const auditTrailQuery = useQuery<AuditTrailRecord[], AxiosError>({
    queryKey: ['auditTrail'],
    refetchOnWindowFocus: false,
    queryFn: async () => await ApiClient.getAuditTrail()
  })

  return {
    patient: patientQuery.data,
    auditTrail: auditTrailQuery.data,
    isLoading: patientQuery.isLoading || auditTrailQuery.isLoading,
    error: patientQuery.error || auditTrailQuery.error
  }
}
