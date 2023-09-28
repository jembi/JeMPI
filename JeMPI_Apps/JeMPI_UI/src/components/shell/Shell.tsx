import { Box, Toolbar } from '@mui/material'

import { Outlet, useLocation } from '@tanstack/react-location'
import ErrorBoundary from '../error/ErrorBoundary'
import NavigationBar from './NavigationBar'

const Shell = () => {
  const location = useLocation()

  return location.current.pathname === '/login' ? (
    <ErrorBoundary>
      <Outlet />
    </ErrorBoundary>
  ) : (
    <Box sx={{ display: 'flex' }}>
      {!new RegExp(/match-details|relink/).test(location.current.pathname) && (
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
