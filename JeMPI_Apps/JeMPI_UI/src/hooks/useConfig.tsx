import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import React, { useMemo } from 'react'
import Loading from '../components/common/Loading'
import ApiErrorMessage from '../components/error/ApiErrorMessage'
import { ApiClient } from '../services/ApiClient'
import getConfig, { Config } from 'config'
import { getApiClient } from '../services/ApiClient'

export interface ConfigContextValue {
  config: Config
  apiClient: ApiClient
}

const ConfigContext = React.createContext<ConfigContextValue | null>(null)
ConfigContext.displayName = 'ConfigContext'

export interface ConfigProviderProps {
  children: React.ReactNode
}

export const ConfigProvider = ({
  children
}: ConfigProviderProps): JSX.Element => {
  const {
    data: config,
    error: configError,
    isLoading: isLoadingConfig,
    isError: isConfigError
  } = useQuery<Config, AxiosError>({
    queryKey: ['config'],
    queryFn: async () => getConfig(),
    refetchOnWindowFocus: false
  })

  const apiClient = useMemo(
    () => config && getApiClient(config),
    [config]
  ) as unknown as ApiClient

  if (isLoadingConfig || !config) {
    return <Loading />
  }

  if (isConfigError) {
    return <ApiErrorMessage error={configError} />
  }
  return (
    <ConfigContext.Provider
      value={{
        config,
        apiClient
      }}
    >
      {children}
    </ConfigContext.Provider>
  )
}

export const useConfig = () => {
  const context = React.useContext(ConfigContext)
  if (!context) {
    throw new Error(`useConfig must be used within an ConfigProvider`)
  }

  return context
}
