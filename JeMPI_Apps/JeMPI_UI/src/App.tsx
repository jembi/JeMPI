import './App.css'

import { CssBaseline, ThemeProvider } from '@mui/material'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import baseTheme from './themes/baseTheme'
import baseRouter from 'router/BaseRouter'
import { ReactQueryDevtools } from 'DevTools'
import { RouterProvider } from 'react-router-dom'

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
        <RouterProvider router={baseRouter} />
        <ReactQueryDevtools />
      </QueryClientProvider>
    </ThemeProvider>
  )
}

export default App
