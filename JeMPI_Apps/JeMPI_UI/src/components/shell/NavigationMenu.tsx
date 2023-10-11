import { AccountCircle as AccountCircleIcon } from '@mui/icons-material'
import { Box, Divider, IconButton, Typography } from '@mui/material'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import { config } from 'config'
import * as React from 'react'
import { useAuth } from '../../hooks/useAuth'
import { useNavigate } from 'react-router-dom'

const NavigationMenu: React.FC = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null)
  const isOpen = Boolean(anchorEl)
  const open = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget)
  }
  const close = () => {
    setAnchorEl(null)
  }
  const handleLogout = () => {
    close()
    logout(navigate)
  }

  if (!user) {
    return null
  }

  return config.useSso ? (
    <>
      <IconButton
        aria-controls={isOpen ? 'basic-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={isOpen ? 'true' : undefined}
        onClick={open}
        size="large"
        edge="end"
      >
        <AccountCircleIcon />
      </IconButton>
      <Menu
        id="basic-menu"
        anchorEl={anchorEl}
        open={isOpen}
        onClose={close}
        MenuListProps={{
          'aria-labelledby': 'basic-button'
        }}
      >
        <MenuItem>
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            <Typography fontWeight={400} fontSize={'16px'}>
              {`${user?.givenName} ${user?.familyName}`}
            </Typography>
            <Typography fontWeight={400} fontSize={'14px'}>
              {user?.email}
            </Typography>
          </Box>
        </MenuItem>
        <Divider sx={{ my: 0.5 }} />
        <MenuItem onClick={handleLogout}>
          <Typography fontWeight={500} fontSize={'13px'}>
            LOGOUT
          </Typography>
        </MenuItem>
      </Menu>
    </>
  ) : (
    <></>
  )
}

export default NavigationMenu
