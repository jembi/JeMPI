import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { ReactLocation, Router } from '@tanstack/react-location'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { SnackbarProvider } from 'notistack'
import ErrorBoundary from './components/error/ErrorBoundary'
import Shell from './components/shell/Shell'
import { AppConfigProvider } from './hooks/useAppConfig'
import { AuthProvider } from './hooks/useAuth'
import baseTheme from './themes/baseTheme'
import baseRoutes from 'routes/BaseRoutes'
import { ReactLocationDevtools, ReactQueryDevtools } from 'DevTools'

const location = new ReactLocation({})
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

const App = () => {
  return (
    <ThemeProvider theme={baseTheme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <Router location={location} routes={baseRoutes}>
          <SnackbarProvider
            anchorOrigin={{ horizontal: 'right', vertical: 'top' }}
          >
            <AuthProvider>
              <ErrorBoundary>
                <AppConfigProvider>
                  <Shell />
                </AppConfigProvider>
              </ErrorBoundary>
            </AuthProvider>
          </SnackbarProvider>
          <ReactLocationDevtools position="bottom-right" />
          <ReactQueryDevtools />
        </Router>
      </QueryClientProvider>
    </ThemeProvider>
  )
}

export default App
