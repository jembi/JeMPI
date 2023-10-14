import { Person } from '@mui/icons-material'
import { Box, Button, Card, Container, Typography } from '@mui/material'
import { AuthChecker, AuthProvider, useAuth } from '../../hooks/useAuth'
import logo from './JeMPI.png'

const LoginPage = () => {
  const { signInWithKeyCloak } = useAuth()

  return (
    <Container
      maxWidth={false}
      sx={{
        background:
          'radial-gradient(65.9% 107.97% at 50% 50%, #8EF680 0%, #377D69 100%)',
        display: 'flex',
        height: '1000px',
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'column',
        gap: '10px'
      }}
    >
      <img alt="JeMPI logo" src={logo} />
      <Card
        sx={{
          minWidth: 400,
          boxShadow: [
            '0px 5px 5px -3px rgba(0, 0, 0, 0.2)',
            '0px 8px 10px 1px rgba(0, 0, 0, 0.14)',
            '0px 3px 14px 2px rgba(0, 0, 0, 0.12)'
          ]
        }}
      >
        <Box p={2}>
          <Typography
            fontStyle="normal"
            fontWeight={400}
            fontSize={24}
            lineHeight={'133.4%'}
          >
            Login
          </Typography>
        </Box>
        <Box
          sx={{
            p: 2,
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            gap: '16px'
          }}
        >
          {/* <TextField
            id="outlined-basic"
            label="Username"
            variant="outlined"
            size="medium"
          />
          <TextField
            id="outlined-basic"
            label="Password"
            variant="outlined"
            size="medium"
            type="password"
          />
          <Button
            variant="contained"
            sx={{
              height: '42px',
              borderColor: theme => theme.palette.primary.main
            }}
          >
            Sign-In
          </Button> */}
          <Button
            startIcon={<Person />}
            variant="outlined"
            onClick={signInWithKeyCloak}
          >
            Sign-In with KeyCloak
          </Button>
          {/* <Link
            fontStyle="normal"
            fontWeight={400}
            fontSize={14}
            lineHeight={'143%'}
          >
            Forgot Password ?
          </Link> */}
        </Box>
      </Card>
    </Container>
  )
}

const login = () => {
  return (
    <AuthChecker>
      <LoginPage></LoginPage>
    </AuthChecker>
    
  )
} 

export default login
