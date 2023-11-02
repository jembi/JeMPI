import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import baseTheme from './themes/baseTheme'
import baseRouter from 'router/BaseRouter'
import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'hooks/useConfig'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import ScrollBackButtons from 'components/shared/ScrollBackButtons'

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
        <ConfigProvider>
          <ScrollBackButtons />
          <RouterProvider router={baseRouter} />
          <ReactQueryDevtools initialIsOpen={false} />
        </ConfigProvider>
      </QueryClientProvider>
    </ThemeProvider>
  )
}

export default App
