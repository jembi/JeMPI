import { Box, Toolbar } from '@mui/material'

import ErrorBoundary from '../error/ErrorBoundary'
import NavigationBar from './NavigationBar'
import { Outlet, useLocation } from 'react-router-dom'

const Shell = () => {
  const location = useLocation()

  return location.pathname === '/login' ? (
    <ErrorBoundary>
      <Outlet />
    </ErrorBoundary>
  ) : (
    <Box sx={{ display: 'flex' }}>
      {!new RegExp(/match-details|relink/).test(location.pathname) && (
        <NavigationBar />
      )}
      <Box
        component="main"
        sx={{
          p: 6,
          width: '100%'
        }}
      >
        <Toolbar />
        <ErrorBoundary>
          <Outlet />
        </ErrorBoundary>
      </Box>
    </Box>
  )
}

export default Shell
