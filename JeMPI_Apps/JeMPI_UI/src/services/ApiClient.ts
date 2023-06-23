import axios, { AxiosRequestConfig } from 'axios'
import { config } from '../config'
import AuditTrailRecord from '../types/AuditTrail'
import { FieldChangeReq, Fields } from '../types/Fields'
import Notification, { NotificationState } from '../types/Notification'
import {
  AnyRecord,
  GoldenRecord as GR,
  PatientRecord as PR
} from '../types/PatientRecord'
import {
  ApiSearchResult,
  CustomSearchQuery,
  SearchQuery
} from '../types/SimpleSearch'
import { OAuthParams, User } from '../types/User'
import ROUTES from './apiRoutes'
import axiosInstance from './axios'
import moxios from './mockBackend'

const client = config.shouldMockBackend ? moxios : axiosInstance

interface NotificationRequest {
  notificationId: string
  state: NotificationState
}

interface LinkRequest {
  goldenID: string
  patientID: string
  newGoldenID?: string
}

interface NotificationResponse {
  count: number
  skippedRecords: number
  records: Notification[]
}

interface GoldenRecordResponse {
  expandedGoldenRecords: GR[]
}

interface GoldenRecord extends Pick<GR, 'sourceId' | 'uid'> {
  demographicData: Omit<GR, 'sourceId' | 'uid'>
}

interface ExpandedGoldenRecord {
  goldenRecord: GoldenRecord
  mpiPatientRecords: Array<ExpandedPatientRecord>
}

interface ExpandedPatientRecord {
  patientRecord: PatientRecord
  score: number
}

interface PatientRecord extends Pick<PR, 'sourceId' | 'uid'> {
  demographicData: Omit<PR, 'sourceId' | 'uid'>
}

class ApiClient {
  async getFields() {
    return await client
      .get<Fields>(ROUTES.GET_FIELDS_CONFIG)
      .then(res => res.data)
  }

  async getMatches(
    limit: number,
    offset: number,
    created: string,
    state: string
  ) {
    return await client
      .get<NotificationResponse>(
        `${ROUTES.GET_NOTIFICATIONS}?limit=${limit}&date=${created}&offset=${offset}&state=${state}`
      )
      .then(res => res.data)
      .then(({ records }) =>
        records.map(record => ({
          ...record,
          created: new Date(record.created)
        }))
      )
  }

  async getAuditTrail() {
    return await client
      .get<AuditTrailRecord[]>(ROUTES.AUDIT_TRAIL)
      .then(res => res.data)
  }

  async getPatientRecord(uid: string) {
    return await client
      .get<PR>(`${ROUTES.PATIENT_RECORD_ROUTE}/${uid}`)
      .then(res => res.data)
      .then((patientRecord: Partial<PatientRecord>) => {
        return {
          ...patientRecord,
          ...patientRecord.demographicData
        }
      })
  }

  async getGoldenRecord(uid: string) {
    return await client
      .get<GR>(`${ROUTES.GOLDEN_RECORD_ROUTE}/${uid}`)
      .then(res => res.data)
      .then(({ goldenRecord, mpiPatientRecords }: any) => {
        return {
          ...goldenRecord,
          ...goldenRecord?.demographicData,
          linkRecords: mpiPatientRecords?.map(
            ({ patientRecord }: Partial<ExpandedPatientRecord>) => {
              return {
                ...patientRecord,
                ...patientRecord?.demographicData
              }
            }
          )
        }
      })
  }

  async getGoldenRecords(uid: string[]) {
    const uids = uid?.map(u => '0x' + parseInt(u).toString(16))
    return await client
      .get<GoldenRecordResponse>(ROUTES.GET_GOLDEN_ID_DOCUMENTS, {
        params: {
          uid: uids
        },
        paramsSerializer: {
          indexes: null
        }
      })
      .then(res => res.data)
      .then((data: any) =>
        data.map(({ goldenRecord }: any) => {
          return {
            ...goldenRecord,
            ...goldenRecord.demographicData
          }
        })
      )
  }

  //TODO Move this logic to the backend and just get match details by notification ID
  async getMatchDetails(uid: string, goldenId: string, candidates: string[]) {
    if (uid === null || uid === '' || typeof uid === 'undefined') {
      return [] as AnyRecord[]
    }
    const patientRecord = this.getPatientRecord(uid)
    const goldenRecord = this.getGoldenRecords([goldenId])
    const candidateRecords = this.getGoldenRecords(candidates)
    return (await axios
      .all<any>([patientRecord, goldenRecord, candidateRecords])
      .then(response => {
        return [{ type: 'Current', ...response[0] }]
          .concat(
            response[1].map((r: AnyRecord) => {
              r.type = 'Golden'
              return r
            })
          )
          .concat(
            response[2].map((r: AnyRecord) => {
              r.type = 'Candidate'
              r.searched = false
              return r
            })
          )
      })) as AnyRecord[]
  }

  async updateNotification(request: NotificationRequest) {
    return await client.post(ROUTES.UPDATE_NOTIFICATION, request).then(res => {
      return res.data
    })
  }

  async newGoldenRecord(request: LinkRequest) {
    return await client
      .patch(
        `${ROUTES.CREATE_GOLDEN_RECORD}?goldenID=${request.goldenID}&patientID=${request.patientID}`
      )
      .then(res => res.data)
  }

  async linkRecord(request: LinkRequest) {
    return await client
      .patch(
        `${ROUTES.LINK_RECORD}?goldenID=${request.goldenID}&newGoldenID=${request.newGoldenID}&patientID=${request.patientID}&score=2`
      )
      .then(res => res.data)
  }

  async searchQuery(
    request: CustomSearchQuery | SearchQuery,
    isGoldenOnly: boolean
  ) {
    const isCustomSearch = '$or' in request
    const endpoint = `${
      isCustomSearch ? ROUTES.POST_CUSTOM_SEARCH : ROUTES.POST_SIMPLE_SEARCH
    }/${isGoldenOnly ? 'golden' : 'patient'}`
    return await client.post(endpoint, request).then(res => {
      if (isGoldenOnly) {
        const { pagination, data } = res.data
        const result: ApiSearchResult = {
          records: {
            data: data.map(
              ({ goldenRecord, mpiPatientRecords }: ExpandedGoldenRecord) => {
                return {
                  ...goldenRecord,
                  ...goldenRecord.demographicData,
                  linkRecords: mpiPatientRecords.map(
                    ({ patientRecord }: ExpandedPatientRecord) => {
                      return {
                        ...patientRecord,
                        ...patientRecord.demographicData
                      }
                    }
                  )
                }
              }
            ),
            pagination: {
              total: pagination.total
            }
          }
        }
        return result
      } else {
        const { pagination, data } = res.data
        const result: ApiSearchResult = {
          records: {
            data: data.map((patientRecord: PatientRecord) => {
              return {
                ...patientRecord,
                ...patientRecord.demographicData
              }
            }),
            pagination: {
              total: pagination.total
            }
          }
        }
        return result
      }
    })
  }

  async getGoldenIds(offset: number, length: number) {
    return await client
      .get<{ goldenIds: string[] }>('gids-paged', {
        params: {
          offset,
          length
        }
      })
      .then(async res => res.data.goldenIds)
  }

  async getExpandedGoldenRecords(
    goldenIds: Array<string> | undefined,
    getPatients: boolean
  ) {
    return await client
      .get<
        Array<{
          uid: string
          record: GoldenRecord | PatientRecord
          type: string
          score: number | null
        }>
      >(ROUTES.EXPANDED_GOLDEN_RECORDS, {
        params: { uidList: goldenIds?.toString() }
      })
      .then(res => res.data)
      .then(data =>
        data.reduce(
          (
            acc: Array<{
              uid: string
              record: GoldenRecord | PatientRecord
              type: string
              score: number | null
            }>,
            curr: any
          ) => {
            const record = {
              ...curr.goldenRecord.demographicData,
              uid: curr.goldenRecord.uid,
              type: 'golden',
              score: null
            }
            if (getPatients) {
              const linkedRecords = curr.interactionsWithScore.map(
                (record: { interaction: PatientRecord; score: number }) => ({
                  ...record.interaction.demographicData,
                  uid: record.interaction.uid,
                  type: 'patient',
                  score: record.score
                })
              )
              acc.push(record, ...linkedRecords)
            } else {
              acc.push(record)
            }
            return acc
          },
          []
        )
      )
  }

  async getGoldenRecordAuditTrail(gid: string) {
    return await client
      .get(ROUTES.GOLDEN_RECORD_AUDIT_TRAIL, {
        params: {
          gid
        }
      })
      .then(res => res.data.entries)
  }

  async getInteractionAuditTrail(iid: string) {
    return await client
      .get(ROUTES.INTERACTION_AUDIT_TRAIL, {
        params: {
          iid
        }
      })
      .then(res => res.data.entries)
  }

  async validateOAuth(oauthParams: OAuthParams) {
    return await client
      .post(ROUTES.VALIDATE_OAUTH, oauthParams)
      .then(res => res.data as User)
  }

  async getCurrentUser() {
    return await client
      .get(ROUTES.CURRENT_USER)
      .then(res => res.data)
      .catch(() => null)
  }

  async logout() {
    return await client.get(ROUTES.LOGOUT)
  }

  async updatedGoldenRecord(uid: string, request: FieldChangeReq) {
    return await client
      .patch(`${ROUTES.UPDATE_GOLDEN_RECORD}/${uid}`, request)
      .then(res => res)
  }

  uploadFile = async (requestConfig: AxiosRequestConfig<FormData>) => {
    await client
      .post(ROUTES.UPLOAD, requestConfig.data, requestConfig)
      .then(res => res.data)
  }
}

export default new ApiClient()
