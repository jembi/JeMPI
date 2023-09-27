import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { config } from '../config'
import AuditTrailRecord from '../types/AuditTrail'
import { FieldChangeReq, Fields } from '../types/Fields'
import {
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
  ExpandedGoldenRecord,
  InteractionWithScore,
  NotificationRequest,
  LinkRequest
} from 'types/BackendResponse'
import { GoldenRecord, PatientRecord, AnyRecord } from 'types/PatientRecord'

const client = config.shouldMockBackend ? moxios : axiosInstance

class ApiClient {
  async getFields() {
    const { data } = await client.get<Fields>(ROUTES.GET_FIELDS_CONFIG)
    return data
  }

  async getMatches(
    limit: number,
    offset: number,
    created: string,
    state: string
  ) {
    const queryParams = {
      limit,
      date: created,
      offset,
      state
    }
    const {
      data: { records, skippedRecords, count }
    } = await client.get<NotificationResponse>(ROUTES.GET_NOTIFICATIONS, {
      params: queryParams
    })
    const formattedRecords = records.map(record => ({
      ...record,
      created: new Date(record.created)
    }))
    const pagination = {
      total: count + skippedRecords
    }
    return {
      records: formattedRecords,
      pagination
    }
  }

  // replaced
  async getAuditTrail() {
    const { data } = await client.get<AuditTrailRecord[]>(ROUTES.AUDIT_TRAIL)
    return data
  }

  async getInteraction(uid: string) {
    const { data: interaction } = await client.get<
      PatientRecord,
      AxiosResponse<Interaction>
    >(`${ROUTES.GET_INTERACTION}/${uid}`)
    return {
      uid: interaction.uid,
      sourceId: interaction.sourceId,
      ...interaction.demographicData
    }
  }

  async getGoldenRecord(uid: string) {
    return await client
      .get<GoldenRecord, AxiosResponse<ExpandedGoldenRecord>>(
        `${ROUTES.GET_GOLDEN_RECORD}/${uid}`
      )
      .then(res => res.data)
      .then(
        ({
          goldenRecord,
          interactionsWithScore
        }: Partial<ExpandedGoldenRecord>) => {
          return {
            ...goldenRecord,
            ...goldenRecord?.demographicData,
            linkRecords: interactionsWithScore?.map(
              ({ interaction, score }: InteractionWithScore) => {
                return {
                  uid: interaction.uid,
                  sourceId: interaction.sourceId,
                  createdAt: interaction.uniqueInteractionData.auxDateCreated,
                  auxId: interaction.uniqueInteractionData.auxId,
                  score,
                  ...interaction?.demographicData
                }
              }
            )
          }
        }
      )
  }

  //TODO Move this logic to the backend and just get match details by notification ID
  async getMatchDetails(uid: string, goldenId: string, candidates: string[]) {
    if (uid === null || uid === '' || typeof uid === 'undefined') {
      return [] as AnyRecord[]
    }
    const patientRecord = this.getInteraction(uid)
    const goldenRecord = this.getGoldenRecord(goldenId)
    const candidateRecords = this.getExpandedGoldenRecords(candidates, false)
    return await axios
      .all<any>([patientRecord, goldenRecord, candidateRecords])
      .then(response => {
        return [
          {
            ...response[0],
            type: 'Current'
          }
        ]
          .concat({
            ...response[1],
            type: 'Golden'
          })
          .concat(
            response[2].map((r: AnyRecord) => ({
              ...r,
              type: 'Candidate'
            }))
          )
      })
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
              ({
                goldenRecord,
                interactionsWithScore
              }: ExpandedGoldenRecord) => {
                return {
                  ...goldenRecord,
                  ...goldenRecord.demographicData,
                  linkRecords: interactionsWithScore.map(
                    ({ interaction }: InteractionWithScore) => {
                      return {
                        uid: interaction.uid,
                        sourceId: interaction.sourceId,
                        ...interaction.demographicData
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
            data: data.map((patientRecord: Interaction) => {
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

  async getFilteredGoldenIds(request: FilterQuery) {
    return await client
      .post<{ data: string[]; pagination: { total: number } }>(
        ROUTES.POST_FILTER_GIDS,
        request
      )
      .then(async res => res.data)
  }

  async getFilteredGoldenIdsWithInteractionCount(request: FilterQuery) {
    return await client
      .post<{
        data: string[]
        interationCount: { total: number }
        pagination: { total: number }
      }>(ROUTES.POST_FILTER_GIDS_WITH_INTERACTION_COUNT, request)
      .then(async res => res.data)
      .then(({ data, interationCount, pagination }) => {
        console.log(data, interationCount)
        return {
          data,
          pagination: { total: pagination.total + interationCount.total }
        }
      })
  }

  async getExpandedGoldenRecords(
    goldenIds: Array<string> | undefined,
    getInteractions: boolean
  ) {
    return await client
      .get<Array<AnyRecord>, AxiosResponse<ExpandedGoldenRecord[]>>(
        ROUTES.GET_EXPANDED_GOLDEN_RECORDS,
        {
          params: { uidList: goldenIds?.toString() }
        }
      )
      .then(res => res.data)
      .then(data =>
        data.reduce((acc: Array<AnyRecord>, curr: ExpandedGoldenRecord) => {
          const record = {
            ...curr.goldenRecord.demographicData,
            uid: curr.goldenRecord.uid,
            createdAt: curr.goldenRecord.uniqueGoldenRecordData.auxDateCreated,
            sourceId: curr.goldenRecord.sourceId,
            type: 'Golden'
          }
          if (getInteractions) {
            const linkedRecords = curr.interactionsWithScore.map(
              ({ interaction, score }: InteractionWithScore) => ({
                ...interaction.demographicData,
                uid: interaction.uid,
                sourceId: interaction.sourceId,
                createdAt: interaction.uniqueInteractionData.auxDateCreated,
                auxId: interaction.uniqueInteractionData.auxId,
                score: score,
                type: 'Current'
              })
            )
            acc.push(
              record,
              ...linkedRecords.sort(
                (objA, objB) => Number(objA.createdAt) - Number(objB.createdAt)
              )
            )
          } else {
            acc.push(record)
          }
          return acc
        }, [])
      )
  }

  async getGoldenRecordAuditTrail(gid: string) {
    const {
      data: { entries }
    } = await client.get(ROUTES.GET_GOLDEN_RECORD_AUDIT_TRAIL, {
      params: {
        gid
      }
    })
    return entries
  }

  async getInteractionAuditTrail(interactionId: string) {
    const {
      data: { entries }
    } = await client.get(ROUTES.GET_INTERACTION_AUDIT_TRAIL, {
      params: {
        iid: interactionId
      }
    })
    return entries
  }

  async validateOAuth(oauthParams: OAuthParams): Promise<User> {
    const { data } = await client.post(ROUTES.VALIDATE_OAUTH, oauthParams)
    return data
  }

  async getCurrentUser() {
    const { data } = await client.get(ROUTES.CURRENT_USER)
    return data
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
