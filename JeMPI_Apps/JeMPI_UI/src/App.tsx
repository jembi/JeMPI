import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import baseTheme from './themes/baseTheme'
import baseRouter from 'router/BaseRouter'
import { ReactQueryDevtools } from 'DevTools'
import { RouterProvider } from 'react-router-dom'
import { AuthProvider } from 'hooks/useAuth'
import { SnackbarProvider } from 'notistack'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

const App = () => {
  return (
    <ThemeProvider theme={baseTheme}>
      <CssBaseline />
      <SnackbarProvider anchorOrigin={{ horizontal: 'right', vertical: 'top' }}>
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <RouterProvider router={baseRouter} />
          </AuthProvider>
          <ReactQueryDevtools />
        </QueryClientProvider>
      </SnackbarProvider>
    </ThemeProvider>
  )
}

export default App
