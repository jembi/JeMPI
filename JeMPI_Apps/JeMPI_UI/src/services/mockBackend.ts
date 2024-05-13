import axios from 'axios'
import AxiosMockAdapter from 'axios-mock-adapter'
import { NotificationState } from '../types/Notification'
import ROUTES from './apiRoutes'

import mockData from './mockData'
import mockFields from './mockFields'

const moxios = axios.create()

const axiosMockAdapterInstance = new AxiosMockAdapter(moxios, {
  delayResponse: 0
})

const {
  notifications,
  patientRecord,
  goldenRecord,
  goldenRecords,
  currentUser,
  searchGoldenRecordResult
} = mockData

axiosMockAdapterInstance
  .onPost(ROUTES.VALIDATE_OAUTH)
  .reply(200, currentUser)
  .onGet(ROUTES.CURRENT_USER)
  .reply(200, currentUser)
  .onPost(ROUTES.POST_NOTIFICATIONS)
  .reply(200, { records: notifications })
  .onPost(new RegExp(`^${ROUTES.POST_INTERACTION}/[A-z0-9]+$`))
  .reply(config => {
    const id = config.url?.split('/').pop()
    if (patientRecord.uid === id) {
      return [200, patientRecord]
    }
    return [404, {}]
  })
  .onPost(new RegExp(`^${ROUTES.POST_GOLDEN_RECORD}/[A-z0-9]+$`))
  .reply(config => {
    const id = config.url?.split('/').pop()
    if (goldenRecord.goldenRecord.uid === id) {
      return [200, goldenRecord]
    }
    return [404, {}]
  })
  .onPost(ROUTES.POST_EXPANDED_GOLDEN_RECORDS)
  .reply(() => {
    // Unique row ids for data grid
    return [200, goldenRecords]
  })
  .onPost(ROUTES.POST_UPDATE_NOTIFICATION)
  .reply(() => {
    notifications[0].status = NotificationState.OPEN
    return [200, notifications]
  })
  .onPatch(new RegExp(`^${ROUTES.POST_IID_NEW_GID_LINK}?.*`))
  .reply(() => {
    return [200, notifications]
  })
  .onPatch(new RegExp(`^${ROUTES.POST_IID_GID_LINK}?.*`))
  .reply(() => {
    return [200, notifications]
  })
  .onPost(ROUTES.POST_FIELDS_CONFIG)
  .reply(200, mockFields)
  .onPost(`${ROUTES.POST_SIMPLE_GOLDEN_SEARCH}`)
  .reply(200, searchGoldenRecordResult)
  .onPost(`${ROUTES.POST_CUSTOM_GOLDEN_SEARCH}`)
  .reply(200, searchGoldenRecordResult)

const sleep = (value: number) =>
  new Promise(resolve => setTimeout(resolve, value))

//Successful upload
axiosMockAdapterInstance.onPost(ROUTES.UPLOAD).reply(async config => {
  const total = 1024 // mocked file size
  for (const progress of [0, 0.2, 0.4, 0.6, 0.8, 1]) {
    await sleep(500)
    if (config.onUploadProgress) {
      config.onUploadProgress({ loaded: total * progress, total, bytes: total })
    }
  }
  return [200, {}]
})

export default moxios
