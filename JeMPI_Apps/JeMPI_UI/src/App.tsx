import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { ReactLocation, Route, Router } from '@tanstack/react-location'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { SnackbarProvider } from 'notistack'
import { lazy } from 'react'
import CustomSearch from './components/customSearch/CustomSearch'
import ErrorBoundary from './components/error/ErrorBoundary'
import NotFound from './components/error/NotFound'
import Import from './components/import/Import'
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
import Records from 'components/browseRecords/BrowseRecords'
import RecordDetails from 'components/recordDetails/RecordDetails'

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
        element: <NotFound />
      },
      {
        path: 'record-details',
        children: [
          {
            path: ':uid',
            children: [
              {
                path: '/',
                element: <RecordDetails />,
                loader: async ({ params }) => ({
                  uid: params.uid
                })
              }
            ]
          }
        ]
      },
      {
        path: 'browse-records',
        element: <Records />
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
      { path: 'import', element: <Import /> }
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
