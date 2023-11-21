import React from 'react'
import Shell from './Shell'
import ErrorBoundary from 'components/error/ErrorBoundary'
import { AppConfigProvider } from 'hooks/useAppConfig'
import { AuthChecker } from 'hooks/useAuth'

const Root = () => {
  return (
    <AuthChecker>
      <ErrorBoundary>
        <AppConfigProvider>
          <Shell />
        </AppConfigProvider>
      </ErrorBoundary>
    </AuthChecker>
  )
}

export default Root
