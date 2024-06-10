import React, { createContext, useContext, useEffect, useState } from 'react'
import { Configuration } from 'types/Configuration'
import { useConfig } from './useConfig'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import Loading from 'components/common/Loading'

type ConfigurationContextType = {
  configuration: Configuration | null
  setConfiguration: React.Dispatch<React.SetStateAction<Configuration | null>>
}

export interface UIConfigurationProviderProps {
  children: React.ReactNode
}

export const ConfigurationContext = createContext<
  ConfigurationContextType | undefined
>(undefined)

export const useConfiguration = () => {
  const context = useContext(ConfigurationContext)
  if (!context) {
    throw new Error(
      'useConfiguration must be used within a ConfigurationProvider'
    )
  }
  return context
}

export const ConfigurationProvider = ({
  children
}: UIConfigurationProviderProps) => {
  const { apiClient } = useConfig()
  const { data, error, isLoading, isError } = useQuery<Configuration>({
    queryKey: ['configuration'],
    queryFn: () => apiClient.fetchConfiguration(),
    refetchOnWindowFocus: false
  })

  const [configuration, setConfiguration] = useState<Configuration | null>(null)

  useEffect(() => {
    if (data) {
      setConfiguration(data)
    }
  }, [data])

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <div>Error: {(error as AxiosError).message}</div>
  }

  if (!configuration) {
    return <div>Loading...</div>
  }

  return (
    <ConfigurationContext.Provider value={{ configuration, setConfiguration }}>
      {children}
    </ConfigurationContext.Provider>
  )
}
