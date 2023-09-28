import { matchByPath, useLocation } from '@tanstack/react-location'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import React, { useCallback, useMemo } from 'react'
import Loading from '../components/common/Loading'
import ApiErrorMessage from '../components/error/ApiErrorMessage'
import ApiClient from '../services/ApiClient'
import { DisplayField, FieldGroup, Fields } from '../types/Fields'
import { AnyRecord } from '../types/PatientRecord'
import { getFieldValueFormatter } from '../utils/formatters'
import { isInputValid } from '../utils/helpers'

export interface AppConfigContextValue {
  availableFields: DisplayField[]
  getFieldsByGroup: (group: FieldGroup) => DisplayField[]
  getPatientName: (patient: AnyRecord) => string
}

const AppConfigContext = React.createContext<AppConfigContextValue | null>(null)
AppConfigContext.displayName = 'AppConfigContext'

export interface AppConfigProviderProps {
  children: React.ReactNode
}

export const AppConfigProvider = ({
  children
}: AppConfigProviderProps): JSX.Element => {
  const location = useLocation()
  const {
    data: fields,
    error,
    isLoading,
    isError
  } = useQuery<Fields, AxiosError>({
    queryKey: ['fields'],
    queryFn: () => ApiClient.getFields(),
    refetchOnWindowFocus: false
  })
  const availableFields: DisplayField[] = useMemo(() => {
    return (fields || [])
      .filter(({ scope }) =>
        scope.some(path => {
          return matchByPath(location.current, { to: path })
        })
      )
      .map(field => {
        return {
          ...field,
          formatValue: getFieldValueFormatter(field.fieldType),
          isValid: (value: unknown) => isInputValid(value, field?.validation)
        }
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fields, location.current])

  const getFieldsByGroup = useCallback(
    (groupName: FieldGroup) => {
      return availableFields.filter(({ groups }) => groups.includes(groupName))
    },
    [availableFields]
  )

  const getPatientName = useCallback(
    (patient: AnyRecord) => {
      return getFieldsByGroup('name')
        .map(({ fieldName }) => {
          return fieldName in patient ? patient[fieldName] : null
        })
        .filter(v => !!v)
        .join(' ')
    },
    [getFieldsByGroup]
  )

  if (isLoading) {
    return <Loading />
  }

  if (isError || !fields) {
    return <ApiErrorMessage error={error} />
  }

  return (
    <AppConfigContext.Provider
      value={{
        availableFields,
        getFieldsByGroup,
        getPatientName
      }}
    >
      {children}
    </AppConfigContext.Provider>
  )
}

export const useAppConfig = () => {
  const context = React.useContext(AppConfigContext)
  if (!context) {
    throw new Error(`useAppConfig must be used within an AppConfigProvider`)
  }
  return context
}
