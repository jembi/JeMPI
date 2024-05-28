import { getApiClient } from 'services/ApiClient'
import configuration from 'services/configurationData'
import mockAxios from 'jest-mock-axios'
import getConfig from 'config'
import ROUTES from 'services/apiRoutes'
import { AxiosResponse } from 'axios'

jest.mock('axios')

test('fetchConfiguration should return configuration data from the server', async () => {
  // Given
  const config = await getConfig()
  const apiClient = getApiClient(config)

  const mockResponse = {
    data: configuration,
    status: 200,
    statusText: 'ok'
  } as AxiosResponse
  mockAxios.post.mockResolvedValue(mockResponse)

  // When
  let result = await apiClient.fetchConfiguration()
  console.log('result')
  // Then
  expect(mockAxios.post).toHaveBeenCalledWith(ROUTES.GET_CONFIGURATION)
  mockAxios.get.mockResolvedValue(mockResponse)

  expect(result).toEqual(configuration)
})
