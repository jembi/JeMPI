import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { config } from '../config'
import { AuditTrailEntries } from '../types/AuditTrail'
import { FieldChangeReq, Fields } from '../types/Fields'
import {
  ApiSearchResponse,
  ApiSearchResult,
  CustomSearchQuery,
  FilterQuery,
  SearchQuery
} from '../types/SimpleSearch'
import { OAuthParams, User } from '../types/User'
import ROUTES from './apiRoutes'
import axiosInstance from './axios'
import moxios from './mockBackend'
import {
  NotificationResponse,
  Interaction,
  ExpandedGoldenRecordResponse,
  InteractionWithScore,
  NotificationRequest,
  LinkRequest,
  GoldenRecordCandidatesResponse
} from 'types/BackendResponse'
import {
  GoldenRecord,
  AnyRecord,
  DemographicData,
  PatientRecord
} from 'types/PatientRecord'
import { Notifications } from 'types/Notification'

const client = config.shouldMockBackend ? moxios : axiosInstance

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
  ): Promise<Notifications> {
    return await client
      .get<NotificationResponse>(
        `${ROUTES.GET_NOTIFICATIONS}?limit=${limit}&date=${created}&offset=${offset}&state=${state}`
      )
      .then(res => res.data)
      .then(({ records, skippedRecords, count }) => ({
        records: records.map(record => ({
          ...record,
          created: new Date(record.created)
        })),
        pagination: {
          total: count + skippedRecords
        }
      }))
  }

  async getInteraction(uid: string) {
    return await client
      .get<Interaction>(`${ROUTES.GET_INTERACTION}/${uid}`)
      .then(res => res.data)
  }

  async getGoldenRecord(uid: string): Promise<GoldenRecord> {
    return await client
      .get<ExpandedGoldenRecordResponse>(`${ROUTES.GET_GOLDEN_RECORD}/${uid}`)
      .then(res => res.data)
      .then(
        ({
          goldenRecord,
          interactionsWithScore
        }: ExpandedGoldenRecordResponse) => {
          return {
            uid: goldenRecord.uid,
            demographicData: goldenRecord.demographicData,
            sourceId: goldenRecord.sourceId,
            type: 'Current',
            createdAt: goldenRecord.uniqueGoldenRecordData.auxDateCreated,
            auxId: goldenRecord.uniqueGoldenRecordData.auxId,
            linkRecords: interactionsWithScore.map(
              ({ interaction, score }: InteractionWithScore) => {
                return {
                  uid: interaction.uid,
                  sourceId: interaction.sourceId,
                  createdAt: interaction.uniqueInteractionData.auxDateCreated,
                  auxId: interaction.uniqueInteractionData.auxId,
                  score,
                  demographicData: interaction?.demographicData
                }
              }
            )
          }
        }
      )
  }

  //TODO Move this logic to the backend and just get match details by notification ID
  async getMatchDetails(
    goldenId: string,
    candidateIds: string[]
  ): Promise<[GoldenRecord, GoldenRecord[]]> {
    const goldenRecord = this.getGoldenRecord(goldenId)
    const candidateRecords = this.getExpandedGoldenRecords(candidateIds)
    const [gr, candidates] = await Promise.all([goldenRecord, candidateRecords])

    return [
      {
        ...gr,
        type: 'Current'
      },
      candidates.map(r => ({
        ...r,
        type: 'Blocked'
      }))
    ]
  }

  async updateNotification(request: NotificationRequest) {
    return await client
      .post(ROUTES.POST_UPDATE_NOTIFICATION, request)
      .then(res => {
        return res.data
      })
  }

  async newGoldenRecord(request: LinkRequest) {
    return await client
      .patch(
        `${ROUTES.PATCH_IID_NEW_GID_LINK}?goldenID=${request.goldenID}&patientID=${request.patientID}`
      )
      .then(res => res.data)
  }

  async linkRecord(request: LinkRequest) {
    return await client
      .patch(
        `${ROUTES.PATCH_IID_GID_LINK}?goldenID=${request.goldenID}&newGoldenID=${request.newGoldenID}&patientID=${request.patientID}&score=2`
      )
      .then(res => res.data)
  }

  async searchQuery(
    request: CustomSearchQuery | SearchQuery,
    isGoldenOnly: boolean
  ): Promise<ApiSearchResult<AnyRecord>> {
    const isCustomSearch = '$or' in request
    const endpoint = `${
      isCustomSearch ? ROUTES.POST_CUSTOM_SEARCH : ROUTES.POST_SIMPLE_SEARCH
    }/${isGoldenOnly ? 'golden' : 'patient'}`
    return await client.post(endpoint, request).then(res => {
      if (isGoldenOnly) {
        const { pagination, data } =
          res.data as ApiSearchResponse<ExpandedGoldenRecordResponse>
        const result: ApiSearchResult<GoldenRecord> = {
          records: {
            data: data.map(
              ({
                goldenRecord,
                interactionsWithScore
              }: ExpandedGoldenRecordResponse) => {
                return {
                  uid: goldenRecord.uid,
                  demographicData: goldenRecord.demographicData,
                  sourceId: goldenRecord.sourceId,
                  createdAt: goldenRecord.uniqueGoldenRecordData.auxDateCreated,
                  auxId: goldenRecord.uniqueGoldenRecordData.auxId,
                  linkRecords: interactionsWithScore.map(
                    ({ interaction, score }: InteractionWithScore) => {
                      return {
                        uid: interaction.uid,
                        sourceId: interaction.sourceId,
                        createdAt:
                          interaction.uniqueInteractionData.auxDateCreated,
                        auxId: interaction.uniqueInteractionData.auxId,
                        score,
                        demographicData: interaction?.demographicData
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
        const { pagination, data } = res.data as ApiSearchResponse<Interaction>
        const result: ApiSearchResult<PatientRecord> = {
          records: {
            data: data.map((interaction: Interaction) => {
              return {
                uid: interaction.uid,
                sourceId: interaction.sourceId,
                createdAt: interaction.uniqueInteractionData.auxDateCreated,
                auxId: interaction.uniqueInteractionData.auxId,
                demographicData: interaction?.demographicData
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

  async getFilteredGoldenIds(request: FilterQuery) {
    return await client
      .post<{ data: string[]; pagination: { total: number } }>(
        ROUTES.POST_FILTER_GIDS,
        request
      )
      .then(res => res.data)
  }

  async getFilteredGoldenIdsWithInteractionCount(request: FilterQuery) {
    return await client
      .post<{
        data: string[]
        interactionCount: { total: number }
        pagination: { total: number }
      }>(ROUTES.POST_FILTER_GIDS_WITH_INTERACTION_COUNT, request)
      .then(res => res.data)
      .then(({ data, interactionCount, pagination }) => {
        return {
          data,
          pagination: { total: pagination.total + interactionCount.total }
        }
      })
  }

  async getExpandedGoldenRecords(
    goldenIds: Array<string> | undefined
  ): Promise<GoldenRecord[]> {
    return await client
      .get<Array<ExpandedGoldenRecordResponse>>(
        ROUTES.GET_EXPANDED_GOLDEN_RECORDS,
        {
          params: { uidList: goldenIds?.toString() }
        }
      )
      .then(res => res.data)
      .then(data =>
        data.map(({ goldenRecord, interactionsWithScore }) => {
          const record: GoldenRecord = {
            demographicData: goldenRecord.demographicData,
            uid: goldenRecord.uid,
            createdAt: goldenRecord.uniqueGoldenRecordData.auxDateCreated,
            sourceId: goldenRecord.sourceId,
            type: 'Current',
            auxId: goldenRecord.uniqueGoldenRecordData.auxId,
            linkRecords: interactionsWithScore
              .map(({ interaction, score }: InteractionWithScore) => ({
                demographicData: interaction.demographicData,
                uid: interaction.uid,
                sourceId: interaction.sourceId,
                createdAt: interaction.uniqueInteractionData.auxDateCreated,
                auxId: interaction.uniqueInteractionData.auxId,
                score: score
              }))
              .sort(
                (objA, objB) => Number(objA.createdAt) - Number(objB.createdAt)
              )
          }
          return record
        })
      )
  }

  async getFlatExpandedGoldenRecords(
    goldenIds: Array<string> | undefined
  ): Promise<AnyRecord[]> {
    const goldrenRecords = await this.getExpandedGoldenRecords(goldenIds)
    return goldrenRecords.reduce((acc: Array<AnyRecord>, record) => {
      acc.push(record, ...record.linkRecords)
      return acc
    }, [])
  }

  async getCandidates(
    demographicData: DemographicData,
    candidateThreshold: number
  ): Promise<GoldenRecord[]> {
    return await client
      .post<GoldenRecordCandidatesResponse>(ROUTES.POST_CR_CANDIDATES, {
        demographicData,
        candidateThreshold
      })
      .then(res =>
        // records needs typing
        res.data.goldenRecords?.map(record => ({
          demographicData: record.demographicData,
          uid: record.goldenId,
          sourceId: record.sourceId,
          createdAt: record.customUniqueGoldenRecordData.auxDateCreated,
          auxId: record.customUniqueGoldenRecordData.auxId,
          score: candidateThreshold,
          type: 'Blocked',
          linkRecords: []
        }))
      )
  }

  async getGoldenRecordAuditTrail(gid: string) {
    return await client
      .get<AuditTrailEntries>(ROUTES.GET_GOLDEN_RECORD_AUDIT_TRAIL, {
        params: {
          gid
        }
      })
      .then(res => res.data.entries)
  }

  async getInteractionAuditTrail(iid: string) {
    return await client
      .get<AuditTrailEntries>(ROUTES.GET_INTERACTION_AUDIT_TRAIL, {
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
      .patch(`${ROUTES.PATCH_GOLDEN_RECORD}/${uid}`, request)
      .then(res => res)
  }

  uploadFile = async (requestConfig: AxiosRequestConfig<FormData>) => {
    await client
      .post(ROUTES.UPLOAD, requestConfig.data, requestConfig)
      .then(res => res.data)
  }
}

export default new ApiClient()
