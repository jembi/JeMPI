import {
  QueryObserverResult,
  RefetchOptions,
  useMutation,
  useQuery,
  useQueryClient
} from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useSnackbar } from 'notistack'
import React, { useEffect, useRef } from 'react'
import LoadingSpinner from '../components/common/LoadingSpinner'

import { config } from 'config'
import ApiErrorMessage from '../components/error/ApiErrorMessage'
import ApiClient from '../services/ApiClient'
import keycloak from '../services/keycloak'
import { OAuthParams, User } from '../types/User'
import { parseQuery } from '../utils/misc'
import { useLocation, useNavigate } from 'react-router-dom'

export interface AuthContextValue {
  user: User | undefined
  isAuthenticated: boolean
  setUser: (data: User | undefined) => void
  logout: () => void
  signInWithKeyCloak: () => void
  refetchUser: (
    options?: RefetchOptions | undefined
  ) => Promise<QueryObserverResult<User, Error>>
  error: Error | null
}

const AuthContext = React.createContext<AuthContextValue | null>(null)
AuthContext.displayName = 'AuthContext'

export interface AuthProviderProps {
  children: React.ReactNode
}

export const AuthProvider = ({ children }: AuthProviderProps): JSX.Element => {
  const queryClient = useQueryClient()
  const location = useLocation()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const oauthRef = useRef<OAuthParams | null>(null)
  const key = 'auth-user'
  const currentUrl = window.location.href
  const isLoginPage = location.pathname === '/login'

  const {
    data: user,
    isLoading,
    error,
    refetch
  } = useQuery<User, AxiosError<unknown, User>>({
    queryKey: [key],
    queryFn: async () => {
      return await ApiClient.getCurrentUser()
    },
    retry: false,
    refetchOnWindowFocus: false,
    enabled: config.useSso
  })

  const { refetch: logout } = useQuery({
    queryKey: ['logout'],
    queryFn: async () => {
      return await ApiClient.logout()
    },
    onSuccess() {
      queryClient.clear()
      navigate({ pathname: '/login' })
    },
    refetchOnWindowFocus: false,
    enabled: false
  })

  const { mutate: validateOAuth } = useMutation({
    mutationFn: ApiClient.validateOAuth,
    onSuccess(response) {
      enqueueSnackbar(`Successfully logged in using KeyCloak`, {
        variant: 'success'
      })
      setUser(response)
      navigate({ pathname: '/' })
    },
    onError() {
      enqueueSnackbar(`Unable to login using KeyCloak`, {
        variant: 'error'
      })
    }
  })

  const setUser = (data: User | undefined) =>
    queryClient.setQueryData([key], data)

  const signInWithKeyCloak = () => {
    keycloak.init({
      onLoad: 'login-required',
      redirectUri: currentUrl,
      checkLoginIframe: false
    })
  }

  useEffect(() => {
    const currentLocation = location
    if (
      config.useSso &&
      !oauthRef.current &&
      isLoginPage &&
      currentLocation.hash
    ) {
      const params = parseQuery(currentLocation.hash) as OAuthParams
      oauthRef.current = params
      validateOAuth(params)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [validateOAuth])

  useEffect(
    () => {
      if (config.useSso) {
        if (!isLoading) {
          if (!user && !isLoginPage) {
            navigate({ pathname: '/login' })
          } else if (user && isLoginPage) {
            navigate({ pathname: '/' })
          }
        }
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [isLoading]
  )

  if (config.useSso && isLoading) {
    return <LoadingSpinner />
  }

  const authContextValue: AuthContextValue = {
    user,
    isAuthenticated: !!user,
    error,
    setUser,
    refetchUser: refetch,
    logout,
    signInWithKeyCloak
  }

  return (
    <AuthContext.Provider value={authContextValue}>
      {error && <ApiErrorMessage error={error} />}
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = React.useContext(AuthContext)
  if (!context) {
    throw new Error(`useAuth must be used within an AuthProvider`)
  }
  return context
}
