import { Box, Container, Divider, Grid, Stack } from '@mui/material'
import PageHeader from 'components/shell/PageHeader'
import CountWidget from './widgets/CountWidget'
import {
  Layers,
  NotificationAdd,
  NotificationsOff,
  Person
} from '@mui/icons-material'
import { pink } from '@mui/material/colors'
import MandU from './widgets/MandU'
import BetaFscore from './widgets/BetaFscore'
import ConfusionMatrix from './widgets/ConfusionMatrix'

const Dashboard = () => {
  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Dashboard'}
        breadcrumbs={[
          {
            link: '/',
            title: 'Dashboard'
          }
        ]}
        description={''}
      />
      <Divider />
      <Stack padding={'2rem 1rem 1rem 1rem'}>
        <Grid container spacing={{ xs: 2, md: 5 }}>
          <Grid item xs={12} lg={6}>
            <Box component="fieldset">
              <legend>Records</legend>
              <Grid container spacing={2}>
                <Grid item xs={12} lg={6}>
                  <CountWidget
                    label="Golden Record"
                    value={10000}
                    icon={<Person sx={{ fontSize: '3.5rem' }} />}
                    iconBackgroundColor={'#FFD700'}
                  />
                </Grid>
                <Grid item xs={12} lg={6}>
                  <CountWidget
                    label="Interactions "
                    value={20000}
                    icon={<Layers sx={{ fontSize: '3.5rem' }} />}
                    iconBackgroundColor={'primary.main'}
                  />
                </Grid>
              </Grid>
            </Box>
          </Grid>
          <Grid item xs={12} lg={6}>
            <Box component="fieldset">
              <legend> Notifications </legend>
              <Grid container spacing={2}>
                <Grid item xs={12} lg={6}>
                  <CountWidget
                    label="New/Open"
                    value={10000}
                    icon={<NotificationAdd sx={{ fontSize: '3.5rem' }} />}
                    iconBackgroundColor={'#76ff03'}
                  />
                </Grid>
                <Grid item xs={12} lg={6}>
                  <CountWidget
                    label="Closed"
                    value={20000}
                    icon={<NotificationsOff sx={{ fontSize: '3.5rem' }} />}
                    iconBackgroundColor={pink[600]}
                  />
                </Grid>
              </Grid>
            </Box>
          </Grid>
          <Grid item xs={12} lg={6}>
            <MandU />
          </Grid>
          <Grid item xs={12} lg={6}>
            <Box component="fieldset" >
              <legend>Confusion Matrix</legend>
              <ConfusionMatrix />
            </Box>
          </Grid>
          <Grid item xs={12} lg={12}>
            <BetaFscore />
          </Grid>
        </Grid>
      </Stack>
    </Container>
  )
}

export default Dashboard
