import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { ReactLocation, Route, Router } from '@tanstack/react-location'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { SnackbarProvider } from 'notistack'
import { lazy } from 'react'
import AuditTrail from './components/auditTrail/AuditTrail'
import CustomSearch from './components/customSearch/CustomSearch'
import ErrorBoundary from './components/error/ErrorBoundary'
import NotFound from './components/error/NotFound'
import HomePage from './components/home/HomePage'
import Import from './components/import/Import'
import LinkedRecords from './components/linkedRecords/LinkedRecords'
import PatientDetails from './components/patient/PatientDetails'
import NotificationWorklist from './components/reviewMatches/NotificationWorklist'
import ReviewLink from './components/reviewMatches/ReviewLink'
import SimpleSearch from './components/search/SimpleSearch'
import SearchResult from './components/searchResult/SearchResult'
import Shell from './components/shell/Shell'
import Login from './components/user/Login'
import { config } from './config'
import { AppConfigProvider } from './hooks/useAppConfig'
import { AuthProvider } from './hooks/useAuth'
import theme from './theme'

const location = new ReactLocation({})
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

const ReactLocationDevtools = !config.isDev
  ? () => null
  : lazy(() =>
      import('@tanstack/react-location-devtools').then(res => ({
        default: res.ReactLocationDevtools
      }))
    )

const routes: Route[] = [
  { path: 'login', element: <Login /> },
  {
    path: '',
    children: [
      {
        path: '/',
        element: <HomePage />
      },
      {
        path: 'notifications',
        children: [
          {
            path: '/',
            element: <NotificationWorklist />
          },
          {
            path: 'match-details',
            element: <ReviewLink />
          }
        ]
      },
      {
        path: 'search',
        children: [
          {
            path: 'simple',
            element: <SimpleSearch />
          },
          {
            path: 'custom',
            element: <CustomSearch />
          }
        ]
      },
      {
        path: 'search-results',
        children: [
          {
            path: 'golden',
            element: (
              <SearchResult isGoldenRecord={true} title="Golden Records Only" />
            )
          },
          {
            path: 'patient',
            element: (
              <SearchResult
                isGoldenRecord={false}
                title="Patient Records Only"
              />
            )
          }
        ]
      },
      { path: 'import', element: <Import /> },
      {
        path: 'golden-record',
        children: [
          {
            path: ':uid',
            children: [
              {
                path: '/',
                element: <PatientDetails isGoldenRecord={true} />,
                loader: async ({ params }) => ({
                  uid: params.uid
                })
              },
              {
                path: 'audit-trail',
                element: <AuditTrail />,
                loader: async ({ params }) => ({
                  uid: params.uid
                })
              },
              {
                path: 'linked-records',
                element: <LinkedRecords />,
                loader: async ({ params }) => ({
                  uid: params.uid
                })
              }
            ]
          }
        ]
      },
      {
        path: 'patient-record',
        children: [
          {
            path: ':uid',
            children: [
              {
                path: '/',
                element: <PatientDetails isGoldenRecord={false} />,
                loader: async ({ params }) => ({
                  uid: params.uid
                })
              }
            ]
          }
        ]
      }
    ]
  },
  { element: <NotFound /> }
]

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <Router location={location} routes={routes}>
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
