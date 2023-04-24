import { Box, Card, Container, Divider, Typography } from '@mui/material'
import Button from '../shared/Button'
import group from './Group.png'

const HomePage = () => {
  return (
    <Container maxWidth={false}>
      <Typography
        variant="h5"
        sx={{
          fontSize: '34px',
          fontWeight: 400,
          color: 'rgba(0, 0, 0, 0.87)'
        }}
      >
        Home
      </Typography>
      <Divider />
      <Card
        sx={{
          marginTop: '33px',
          background: '#FFFFFF',
          boxShadow: '0px 0px 0px 1px #E0E0E0',
          borderRadius: '4px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        <Box
          sx={{
            py: '50px',
            px: '15px',
            maxWidth: 'sm',
            display: 'flex',
            flexDirection: 'column',
            gap: '20px'
          }}
        >
          <Typography
            fontWeight={300}
            fontSize={'60px'}
            lineHeight={'120%'}
            color={'rgba(0, 0, 0, 0.6)'}
          >
            Welcome to JeMPI
          </Typography>
          <img alt="home" width={'100%'} height={'100%'} src={group} />
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: '8px'
            }}
          >
            <Typography
              fontWeight={500}
              fontSize={20}
              lineHeight={'160%'}
              letterSpacing={'0.15px'}
              color={'rgba(0, 0, 0, 0.87)'}
            >
              JeMPI, is a Master Patient Index (MPI) or Client Registry (CR).
            </Typography>
            <Typography
              fontWeight={400}
              fontSize={16}
              lineHeight={'150%'}
              letterSpacing={'0.15px'}
              color={'rgba(0, 0, 0, 0.87)'}
            >
              An MPI that links electronic medical records (EMRs) across
              multiple facilities in the system by storing demographic
              information in a centralised database of each patient who receives
              healthcare services, along with links to medical records for each
              facility.
            </Typography>
            <Typography
              fontWeight={400}
              fontSize={16}
              lineHeight={'150%'}
              letterSpacing={'0.15px'}
              color={'rgba(0, 0, 0, 0.87)'}
            >
              JeMPI matches and links records from multiple facilities by
              uniquely identifying individuals to maintain accurate patient
              information. A central point can query patient demographics and
              return medical records from all system facilities.
            </Typography>
          </Box>
          <Box
            sx={{
              display: 'flex',
              gap: '4px'
            }}
          >
            <Button href="/search/simple" variant="contained">
              Search
            </Button>
          </Box>
        </Box>
      </Card>
    </Container>
  )
}

export default HomePage
