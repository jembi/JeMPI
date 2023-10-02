import React from 'react'
import Shell from './Shell'
import ErrorBoundary from 'components/error/ErrorBoundary'
import { AppConfigProvider } from 'hooks/useAppConfig'
import { AuthProvider } from 'hooks/useAuth'
import { SnackbarProvider } from 'notistack'

const Root = () => {
  return (
    <SnackbarProvider anchorOrigin={{ horizontal: 'right', vertical: 'top' }}>
      <AuthProvider>
        <ErrorBoundary>
          <AppConfigProvider>
            <Shell />
          </AppConfigProvider>
        </ErrorBoundary>
      </AuthProvider>
    </SnackbarProvider>
  )
}

export default Root
