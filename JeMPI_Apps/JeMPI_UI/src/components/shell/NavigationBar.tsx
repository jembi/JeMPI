import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  Search as SearchIcon
} from '@mui/icons-material'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import { AppBar, Box, Toolbar } from '@mui/material'
import Button from '../shared/Button'
import JembiLogo from './JembiLogo'
import NavigationMenu from './NavigationMenu'

const colorGradient =
  'linear-gradient(90.05deg, #8BF280 -4.51%, #3B826B -4.5%, #58AB73 99.95%)'

const NavigationBar = () => {
  return (
    <AppBar position="fixed">
      <Toolbar>
        <JembiLogo />
        <Box sx={{ flexGrow: 1 }}>
          <Button href="/" startIcon={<DashboardIcon />} className="Appbar">
            Dashboard
          </Button>
          <Button
            href="/browse-records"
            startIcon={<SearchIcon />}
            className="Appbar"
          >
            Browse
          </Button>
          <Button
            href="/notifications"
            startIcon={<PeopleIcon />}
            className="Appbar"
          >
            Notifications
          </Button>
          <Button
            href="/import"
            startIcon={<UploadFileIcon />}
            className="Appbar"
          >
            IMPORT
          </Button>
        </Box>
        <NavigationMenu />
      </Toolbar>
      <Box
        sx={{
          background: '#274263',
          height: 12
        }}
      />
    </AppBar>
  )
}

export default NavigationBar
