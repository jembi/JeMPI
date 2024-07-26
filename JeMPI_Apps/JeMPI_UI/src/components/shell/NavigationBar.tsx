import { useState } from 'react'
import {
  AppBar,
  Box,
  Divider,
  Drawer,
  IconButton,
  Toolbar
} from '@mui/material'
import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  Search as SearchIcon
} from '@mui/icons-material'
import MenuIcon from '@mui/icons-material/Menu'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import JembiLogo from './JembiLogo'
import NavigationMenu from './NavigationMenu'
import { Link } from 'react-router-dom'
import { useConfig } from 'hooks/useConfig'

const LabeledIconBox = ({
  icon,
  label,
  link
}: {
  icon: JSX.Element
  label: string
  link: string
}) => (
  <Link to={`${link}`} className="unset-decoration">
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: 'repeat(2, auto)',
        fontWeight: '500',
        justifyContent: 'start',
        width: '100%',
        gap: 1
      }}
    >
      {icon}
      {label}
    </Box>
  </Link>
)

const drawerWidth = 240

const navigationItems = [
  { icon: <DashboardIcon />, label: 'Dashboard', link: '/' },
  { icon: <SearchIcon />, label: 'Browse', link: '/browse-records' },
  { icon: <PeopleIcon />, label: 'Notifications', link: '/notifications' },
  { icon: <UploadFileIcon />, label: 'Import', link: '/import' }
]

const NavigationBar = () => {
  const container = window?.document?.body
  const { config } = useConfig()
  const [mobileOpen, setMobileOpen] = useState(false)

  const handleDrawerToggle = () => {
    setMobileOpen(prevState => !prevState)
  }

  const drawer = (
    <>
      {config.showBrandLogo && (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center'
          }}
        >
          <JembiLogo />
        </div>
      )}
      <Divider />
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(1, auto)',
          fontWeight: '500',
          justifyContent: 'start',
          alignItems: 'end',
          gap: 3,
          padding: '2rem 1rem 1rem 1rem'
        }}
      >
        {navigationItems.map(item => (
          <LabeledIconBox
            key={item.label} 
            icon={item.icon}
            label={item.label}
            link={item.link}
          />
        ))}
      </Box>
      <NavigationMenu />
    </>
  )

  return (
    <>
      <nav>
        <Drawer
          container={container}
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true // Better open performance on mobile.
          }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth
            }
          }}
        >
          {drawer}
        </Drawer>
      </nav>
      <AppBar position="fixed">
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{
              mr: 2,
              display: { xs: 'block', md: 'none' },
              color: '#000'
            }}
          >
            <MenuIcon />
          </IconButton>
          <Box
            sx={{
              display: { xs: 'grid', md: 'block' },
              justifyContent: { xs: 'center', md: 'start' },
              width: { xs: '100%', md: 'auto' }
            }}
          >
            {config.showBrandLogo && <JembiLogo />}
          </Box>
          <Box
            sx={{
              display: { xs: 'none', md: 'flex' },
              fontWeight: '500',
              justifyContent: 'start',
              alignItems: 'end',
              width: '100%',
              gap: 3
            }}
          >
            {navigationItems.map((item, index) => (
              <Box
                key={item.label}
                sx={{
                  ml: index < navigationItems.length - 1 ? 0 : 'auto'
                }}
              >
                <LabeledIconBox
                  icon={item.icon}
                  label={item.label}
                  link={item.link}
                />
              </Box>
            ))}
          </Box>
          <NavigationMenu />
        </Toolbar>
        <Box
          sx={{
            background: '#274263',
            height: 3
          }}
        />
      </AppBar>
    </>
  )
}

export default NavigationBar
