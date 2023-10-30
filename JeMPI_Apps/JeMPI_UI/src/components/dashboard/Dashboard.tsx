import { Box, Container, Divider, Grid, Stack, Tab, Tabs } from '@mui/material'
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
import BetaFscore from './widgets/BetaFscoreWidget'
import ConfusionMatrix from './widgets/ConfusionMatrix'
import { useState } from 'react'

interface TabPanelProps {
  children?: React.ReactNode
  index: number
  value: number
}

function CustomTabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && children}
    </div>
  )
}

function a11yProps(index: number) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`
  }
}

const Dashboard = () => {
  const [value, setValue] = useState(0)

  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }
  return (
    <Container maxWidth={false}>
      <Stack padding={{ lg: '2rem 1rem 1rem 1rem', xs: '1rem 0rem 0rem 0rem' }}>
        <Tabs
          value={value}
          onChange={handleChange}
          aria-label="basic tabs example"
        >
          <Tab label="Confusion Matrix" {...a11yProps(0)} />
          <Tab label="M & U Values" {...a11yProps(1)} />
        </Tabs>
        <CustomTabPanel value={value} index={0}>
          <Grid container spacing={{ xs: 2, md: 5 }}>
            <Grid item xs={12} lg={6}>
              <Grid container>
                <Grid item xs={12}>
                  <Box component="fieldset">
                    <legend>Records</legend>
                    <Grid container spacing={2}>
                      <Grid item xs={6}>
                        <CountWidget
                          label="Golden Record"
                          value={10000}
                          icon={<Person sx={{ fontSize: '3.5rem' }} />}
                          iconBackgroundColor={'#FFD700'}
                        />
                      </Grid>
                      <Grid item xs={6}>
                        <CountWidget
                          label="Interactions "
                          value={20000}
                          icon={<Layers sx={{ fontSize: '3.5rem' }} />}
                          iconBackgroundColor={'primary.main'}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <Box component="fieldset">
                          <legend> Notifications </legend>
                          <Grid container spacing={2}>
                            <Grid item xs={6}>
                              <CountWidget
                                label="New/Open"
                                value={10000}
                                icon={
                                  <NotificationAdd
                                    sx={{ fontSize: '3.5rem' }}
                                  />
                                }
                                iconBackgroundColor={'#76ff03'}
                              />
                            </Grid>
                            <Grid item xs={6}>
                              <CountWidget
                                label="Closed"
                                value={20000}
                                icon={
                                  <NotificationsOff
                                    sx={{ fontSize: '3.5rem' }}
                                  />
                                }
                                iconBackgroundColor={pink[600]}
                              />
                            </Grid>
                          </Grid>
                        </Box>
                      </Grid>
                      <Grid item xs={12}>
                        <BetaFscore />
                      </Grid>
                    </Grid>
                  </Box>
                </Grid>
              </Grid>
            </Grid>
            <Grid item xs={12} lg={6}>
              <Box component="fieldset">
                <legend>Confusion Matrix</legend>
                <ConfusionMatrix />
              </Box>
            </Grid>
          </Grid>
        </CustomTabPanel>

        <CustomTabPanel value={value} index={1}>
          <Grid item xs={12} lg={6}>
            <MandU />
          </Grid>
        </CustomTabPanel>
      </Stack>
    </Container>
  )
}

export default Dashboard
