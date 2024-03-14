import { AxiosInstance, AxiosRequestConfig } from 'axios'
import { AuditTrail } from '../types/AuditTrail'
import { FieldChangeReq, Fields } from '../types/Fields'
import {
  ApiSearchResponse,
  ApiSearchResult,
  CustomSearchQuery,
  FilterQuery,
  SearchQuery
} from '../types/SimpleSearch'
import { OAuthParams } from '../types/User'
import ROUTES from './apiRoutes'
import moxios from './mockBackend'
import {
  NotificationResponse,
  Interaction,
  ExpandedGoldenRecordResponse,
  InteractionWithScore,
  NotificationRequest,
  LinkRequest,
  GoldenRecordCandidatesResponse,
  DashboardData
} from 'types/BackendResponse'
import {
  GoldenRecord,
  AnyRecord,
  DemographicData,
  PatientRecord
} from 'types/PatientRecord'
import { Notifications } from 'types/Notification'
import { Config } from 'config'
import axios from 'axios'
import { getCookie } from '../utils/misc'

const apiClientAuth = (() => {
  const authKey = 'jempi-auth-key'
  return {
    clearAuthToken: async () => {
      await localStorage.removeItem(authKey)
    },
    getAuthToken: async () => {
      return await localStorage.getItem(authKey)
    },
    setAuthToken: async (token: string) => {
      await localStorage.setItem(authKey, token)
    }
  }
})()

export class ApiClient {
  client!: AxiosInstance

  async init(config: Config) {
    if (process.env.NODE_ENV === 'test') {
      this.client = moxios
    } else {
      const axiosInstance = axios.create({
        withCredentials: true,
        baseURL: config.apiUrl
          ? `${config.apiUrl}/JeMPI`
          : `${window.location.protocol}//${window.location.hostname}:${process.env.REACT_APP_JEMPI_BASE_API_PORT}/JeMPI`,
        responseType: 'json'
      })

      // Add a request interceptor to set CSRF and update loading state
      axiosInstance.interceptors.request.use(async request => {
        const { method } = request
        if (['post', 'patch', 'put', 'delete'].indexOf(method || '') !== -1) {
          const csrfToken = getCookie('XSRF-TOKEN')
          if (csrfToken && request.headers) {
            request.headers['X-XSRF-TOKEN'] = csrfToken
          }
        }

        const authToken = await apiClientAuth.getAuthToken()
        if (authToken && request.headers) {
          request.headers['authorization'] = authToken
        }

        return request
      })
      this.client = axiosInstance
    }
  }

  async getFields() {
    const { data } = await this.client.get<Fields>(ROUTES.GET_FIELDS_CONFIG)
    return data
  }

  async getMatches(
    limit: number,
    offset: number,
    startDay: string,
    endDay: string,
    states: string[]
  ): Promise<Notifications> {
    const url = `${ROUTES.GET_NOTIFICATIONS}?limit=${limit}&startDate=${startDay}&endDate=${endDay}&offset=${offset}&states=${states}`
    const { data } = await this.client.get<NotificationResponse>(url)
    const { records, skippedRecords, count } = data

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

  async getDashboardData() {
    const { data } = await this.client.get<DashboardData>(
      ROUTES.GET_DASHBOARD_DATA
    )
    return data
  }

  async getInteraction(uid: string) {
    const { data } = await this.client.get<Interaction>(
      `${ROUTES.GET_INTERACTION}/${uid}`
    )
    return data
  }

  async getGoldenRecord(uid: string): Promise<GoldenRecord> {
    const {
      data: { goldenRecord, interactionsWithScore }
    } = await this.client.get<ExpandedGoldenRecordResponse>(
      `${ROUTES.GET_GOLDEN_RECORD}/${uid}`
    )
    return {
      uid: goldenRecord.uid,
      demographicData: goldenRecord.demographicData,
      sourceId: goldenRecord.sourceId,
      type: 'Current',
      createdAt: goldenRecord.uniqueGoldenRecordData.auxDateCreated,
      auxId: goldenRecord.uniqueGoldenRecordData.auxId,
      linkRecords: interactionsWithScore.map(({ interaction, score }) => ({
        uid: interaction.uid,
        sourceId: interaction.sourceId,
        createdAt: interaction.uniqueInteractionData.auxDateCreated,
        auxId: interaction.uniqueInteractionData.auxId,
        score,
        demographicData: interaction?.demographicData
      }))
    }
  }

  //TODO Move this logic to the backend and just get match details by notification ID
  async getMatchDetails(
    goldenId: string,
    candidateIds: string[]
  ): Promise<[GoldenRecord, GoldenRecord[]]> {
    const [goldenRecord, candidateRecords] = await Promise.all([
      this.getGoldenRecord(goldenId),
      this.getExpandedGoldenRecords(candidateIds)
    ])
    return [
      {
        ...goldenRecord,
        type: 'Current'
      },
      candidateRecords.map(r => ({
        ...r,
        type: 'Blocked'
      }))
    ]
  }

  async updateNotification(request: NotificationRequest) {
    const { data } = await this.client.post(
      ROUTES.POST_UPDATE_NOTIFICATION,
      request
    )
    return data
  }

  async newGoldenRecord(request: LinkRequest) {
    const { data } = await this.client.post<LinkRequest>(ROUTES.POST_IID_NEW_GID_LINK, request)
    return data
  }

  async linkRecord(linkRequest: LinkRequest) {
    const { data } = await this.client.post<LinkRequest>(ROUTES.POST_IID_GID_LINK, linkRequest)
    return data
  }

  async searchQuery(
    request: CustomSearchQuery | SearchQuery,
    isGoldenOnly: boolean
  ): Promise<ApiSearchResult<AnyRecord>> {
    const isCustomSearch = '$or' in request
    const endpoint = `${
      isCustomSearch ? ROUTES.POST_CUSTOM_SEARCH : ROUTES.POST_SIMPLE_SEARCH
    }/${isGoldenOnly ? 'golden' : 'patient'}`
    const { data: querySearchResponse } = await this.client.post(
      endpoint,
      request
    )
    if (isGoldenOnly) {
      const { pagination, data } =
        querySearchResponse as ApiSearchResponse<ExpandedGoldenRecordResponse>
      const result: ApiSearchResult<GoldenRecord> = {
        records: {
          data: data.map(({ goldenRecord, interactionsWithScore }) => ({
            uid: goldenRecord.uid,
            demographicData: goldenRecord.demographicData,
            sourceId: goldenRecord.sourceId,
            createdAt: goldenRecord.uniqueGoldenRecordData.auxDateCreated,
            auxId: goldenRecord.uniqueGoldenRecordData.auxId,
            linkRecords: interactionsWithScore.map(
              ({ interaction, score }) => ({
                uid: interaction.uid,
                sourceId: interaction.sourceId,
                createdAt: interaction.uniqueInteractionData.auxDateCreated,
                auxId: interaction.uniqueInteractionData.auxId,
                score,
                demographicData: interaction?.demographicData
              })
            )
          })),
          pagination: {
            total: pagination.total
          }
        }
      }
      return result
    } else {
      const { pagination, data } =
        querySearchResponse as ApiSearchResponse<Interaction>
      const result: ApiSearchResult<PatientRecord> = {
        records: {
          data: data.map((interaction: Interaction) => ({
            uid: interaction.uid,
            sourceId: interaction.sourceId,
            createdAt: interaction.uniqueInteractionData.auxDateCreated,
            auxId: interaction.uniqueInteractionData.auxId,
            demographicData: interaction?.demographicData
          })),
          pagination: {
            total: pagination.total
          }
        }
      }
      return result
    }
  }

  async getFilteredGoldenIds(request: FilterQuery) {
    const { data } = await this.client.post<{
      data: string[]
      pagination: { total: number }
    }>(ROUTES.POST_FILTER_GIDS, request)
    return data
  }

  async getFilteredGoldenIdsWithInteractionCount(request: FilterQuery) {
    const {
      data: { data, interactionCount, pagination }
    } = await this.client.post<{
      data: string[]
      interactionCount: { total: number }
      pagination: { total: number }
    }>(ROUTES.POST_FILTER_GIDS_WITH_INTERACTION_COUNT, request)
    const total = pagination.total + interactionCount.total
    return {
      data,
      pagination: { total }
    }
  }

  async getExpandedGoldenRecords(
    goldenIds: Array<string> | undefined
  ): Promise<GoldenRecord[]> {
    const { data } = await this.client.get<Array<ExpandedGoldenRecordResponse>>(
      ROUTES.GET_EXPANDED_GOLDEN_RECORDS,
      {
        params: { uidList: goldenIds?.toString() }
      }
    )
    const records: GoldenRecord[] = data.map(
      ({ goldenRecord, interactionsWithScore }) => {
        return {
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
              score
            }))
            .sort(
              (objA, objB) => Number(objA.createdAt) - Number(objB.createdAt)
            )
        }
      }
    )

    return records
  }

  async getFlatExpandedGoldenRecords(
    goldenIds: Array<string> | undefined
  ): Promise<AnyRecord[]> {
    const goldenRecords = await this.getExpandedGoldenRecords(goldenIds)
    return goldenRecords.reduce((acc: Array<AnyRecord>, record) => {
      acc.push(record, ...record.linkRecords)
      return acc
    }, [])
  }

  async getCandidates(
    demographicData: DemographicData,
    candidateThreshold: number
  ): Promise<GoldenRecord[]> {
    const { data } = await this.client.post<GoldenRecordCandidatesResponse>(
      ROUTES.POST_CR_CANDIDATES,
      { demographicData, candidateThreshold }
    )

    return data.goldenRecords?.map(record => ({
      demographicData: record.demographicData,
      uid: record.goldenId,
      sourceId: record.sourceId,
      createdAt: record.customUniqueGoldenRecordData.auxDateCreated,
      auxId: record.customUniqueGoldenRecordData.auxId,
      score: candidateThreshold,
      type: 'Blocked',
      linkRecords: []
    }))
  }

  async getGoldenRecordAuditTrail(gid: string) {
    const {
      data
    } = await this.client.get<Array<AuditTrail>>(
      ROUTES.GET_GOLDEN_RECORD_AUDIT_TRAIL,
      {
        params: {
          gid
        }
      }
    )
    return data
  }

  async getInteractionAuditTrail(iid: string) {
    const {
      data
    } = await this.client.get<Array<AuditTrail>>(
      ROUTES.GET_INTERACTION_AUDIT_TRAIL,
      {
        params: {
          iid
        }
      }
    )
    return data
  }

  async validateOAuth(oauthParams: OAuthParams) {
    const response = await this.client.post(ROUTES.VALIDATE_OAUTH, oauthParams)
    if (
      response.status == 200 &&
      response.data &&
      'set-authorization' in response.headers
    ) {
      await apiClientAuth.setAuthToken(
        response.headers['set-authorization'] as string
      )
      return response.data
    }
    throw new Error(
      `Got response from server but not all authentication details were present. Failed to validate`
    )
  }

  async getCurrentUser() {
    const { data } = await this.client.get(ROUTES.CURRENT_USER)
    return typeof data === 'string' && data.length === 0 ? null : data
  }

  async logout() {
    const response = await this.client.get(ROUTES.LOGOUT)
    if (response.status == 200) {
      await apiClientAuth.clearAuthToken()
    }
    return response
  }

  async updatedGoldenRecord(uid: string, request: FieldChangeReq) {
    const response = await this.client.patch(
      `${ROUTES.PATCH_GOLDEN_RECORD}/${uid}`,
      request
    )
    return response
  }

  uploadFile = async (requestConfig: AxiosRequestConfig<FormData>) => {
    const { data } = await this.client.post(
      ROUTES.UPLOAD,
      requestConfig.data,
      requestConfig
    )
    return data
  }
}

const apiClient = new ApiClient()

export function getApiClient(config: Config) {
  apiClient.init(config)
  return apiClient
}
