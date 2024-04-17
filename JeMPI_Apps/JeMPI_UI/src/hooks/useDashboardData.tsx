import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import React from 'react'
import { useConfig } from './useConfig'
import { DashboardData } from 'types/BackendResponse'
import { useSnackbar } from 'notistack'


export interface DashboardDataContextValue {
    data: DashboardData | undefined,
    isLoading: boolean,
    isError: boolean,
    isReady: boolean
}
    

const DashboardDataContext = React.createContext<DashboardDataContextValue | null>(null)
DashboardDataContext.displayName = 'DashboardDataContext'

export interface DashboardDataProviderProps {
  children: React.ReactNode
}

export const DashboardDataProvider = ({
  children
}: DashboardDataProviderProps): JSX.Element => {

    const { apiClient, config } = useConfig()
    const { enqueueSnackbar } = useSnackbar()

  const {
    data: dashboardData,
    error: dashboardDataError,
    isLoading,
    isError
  } = useQuery<DashboardData, AxiosError>({
    queryKey: ['dashboardData'],
    queryFn: async () =>  apiClient.fetchDashboardData().then(r => {
            r.dashboardData = JSON.parse(r.dashboardData)
            return r
        }),
    refetchInterval: config.refetchInterval,
    cacheTime: config.cacheTime,
    staleTime: config.staleTime,
  })


  if (isError) {
    enqueueSnackbar(`Unable to get dashboard data`, {
        variant: 'error'
      })
    console.error(dashboardDataError)
  }

  return (
    <DashboardDataContext.Provider
      value={{
        data: dashboardData,
        isLoading,
        isError,
        isReady: !(isLoading || isError)
      }}
    >
      {children}
    </DashboardDataContext.Provider>
  )
}

export const useDashboardData = () => {
  const context = React.useContext(DashboardDataContext)
  if (!context) {
    throw new Error(`useConfig must be used within an ConfigProvider`)
  }

  return context
}
