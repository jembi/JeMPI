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
import { useLocation, useNavigate, NavigateFunction } from 'react-router-dom'

export interface AuthContextValue {
  currentUser: User | undefined
  isAuthenticated: boolean
  setUser: (data: User | undefined) => void
  logout: (navigate: NavigateFunction) => void
  signInWithKeyCloak: () => void
  refetchUser: (
    options?: RefetchOptions | undefined
  ) => Promise<QueryObserverResult<User, Error>>
  isLoading: boolean
  error: AxiosError | null
}

const AuthContext = React.createContext<AuthContextValue | null>(null)
AuthContext.displayName = 'AuthContext'

export interface AuthProviderProps {
  children: React.ReactNode
}

export const AuthChecker = ({ children }: AuthProviderProps): JSX.Element => {
  const authContext = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const isLoginPage = location.pathname === '/login'
  const { enqueueSnackbar } = useSnackbar()
  const oauthRef = useRef<OAuthParams | null>(null)

  const { mutate: validateOAuth } = useMutation({
    mutationFn: ApiClient.validateOAuth,
    onSuccess(response) {
      enqueueSnackbar(`Successfully logged in using KeyCloak`, {
        variant: 'success'
      })
      authContext.setUser(response)
      navigate({ pathname: '/' })
    },
    onError() {
      enqueueSnackbar(`Unable to login using KeyCloak`, {
        variant: 'error'
      })
    }
  })

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
      if (config.useSso && !authContext.isLoading) {
        if (!authContext.currentUser && !isLoginPage) {
          navigate({ pathname: '/login' });
        } else if (authContext.currentUser && isLoginPage) {
          navigate({ pathname: '/' });
        }
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [authContext.isLoading] //TODO: Check
  )

  if (config.useSso && authContext.isLoading) {
    return <LoadingSpinner id="user-loading-spinner" />
  }

  if (
    config.useSso &&
    !authContext.isLoading &&
    !authContext.isAuthenticated &&
    !isLoginPage
  ) {
    return (<>
      {authContext.error && <ApiErrorMessage error={authContext.error} />}
      <LoadingSpinner id="user-loading-spinner" />
    </>)
  }

  return (<>
    {authContext.error && <ApiErrorMessage error={authContext.error} />}
    {children}
  </>)
}

const currentUserOueryClientKey = 'auth-user'

export const AuthProvider = ({ children }: AuthProviderProps): JSX.Element => {
  const queryClient = useQueryClient()
  const { enqueueSnackbar } = useSnackbar()

  const {
    data: currentUser,
    isLoading,
    error,
    refetch
  } = useQuery<User, AxiosError<unknown, User>>({
    queryKey: [currentUserOueryClientKey],
    queryFn: async () => {
      return await ApiClient.getCurrentUser()
    },
    retry: false,
    refetchOnWindowFocus: false,
    enabled: config.useSso
  })

  const { mutate: logout } = useMutation({
    mutationFn: ApiClient.logout,
    onSuccess(data: any, navigate: NavigateFunction) {
      queryClient.clear()
      navigate({ pathname: '/login' })
    },
    onError() {
      enqueueSnackbar(`Error occured logging out`, {
        variant: 'error'
      })
    }
  })

  const setUser = (data: User | undefined) => {
    queryClient.setQueryData([currentUserOueryClientKey], data)
  }

  const signInWithKeyCloak = () => {
    keycloak.init({
      onLoad: 'login-required',
      redirectUri: window.location.href,
      checkLoginIframe: false
    })
  }

  // No need to display this, as we redirect user to the login page, if that is the case
  const filterForbiddenErrors = (error: any) => {
    if (!error) {
      return null
    }
    if (error instanceof AxiosError) {
      if (error.response?.status == 403) {
        return null
      }
    }
    return error
  }

  const authContextValue: AuthContextValue = {
    currentUser,
    isAuthenticated: !!currentUser,
    error: filterForbiddenErrors(error),
    setUser,
    refetchUser: refetch,
    logout: (navigate: NavigateFunction) => {
      logout(navigate)
    },
    signInWithKeyCloak,
    isLoading
  }

  return (
    <AuthContext.Provider value={authContextValue}>
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
