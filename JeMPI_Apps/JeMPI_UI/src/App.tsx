import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import baseTheme from './themes/baseTheme'
import baseRouter from 'router/BaseRouter'
import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'hooks/useConfig'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import ScrollBackButtons from 'components/shared/ScrollBackButtons'
import { AuthProvider } from 'hooks/useAuth'
import { SnackbarProvider } from 'notistack'
import { ConfigurationProvider } from 'hooks/useUIConfiguration'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * (60 * 1000),
      cacheTime: 10 * (60 * 1000)
    }
  }
})

const App = () => {
  return (
    <ThemeProvider theme={baseTheme}>
      <CssBaseline />
      <SnackbarProvider anchorOrigin={{ horizontal: 'right', vertical: 'top' }}>
        <QueryClientProvider client={queryClient}>
          <ConfigProvider>
            <ConfigurationProvider>
              <AuthProvider>
                <ScrollBackButtons />
                <RouterProvider router={baseRouter} />
              </AuthProvider>
              <ReactQueryDevtools initialIsOpen={false} />
            </ConfigurationProvider>
          </ConfigProvider>
        </QueryClientProvider>
      </SnackbarProvider>
    </ThemeProvider>
  )
}

export default App
