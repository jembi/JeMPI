import {
  Box,
  Container,
  Grid,
  Stack,
  Tab,
  Tabs,
  Typography
} from '@mui/material'
import CountWidget from './widgets/CountWidgetWidget'
import {
  Layers,
  NotificationAdd,
  NotificationsOff,
  Person
} from '@mui/icons-material'
import { pink } from '@mui/material/colors'
import MandU from './widgets/MandUWidget'
import BetaFscore from './widgets/BetaFscoreWidget'
import ConfusionMatrix from './widgets/ConfusionMatrixWidget'
import { useEffect, useState } from 'react'
import { ImportProcessWidget } from './widgets/ImportProcessWidget'
import { useDashboardData } from 'hooks/useDashboardData'
import { useConfig } from 'hooks/useConfig'
import { useQuery } from '@tanstack/react-query'
import { Configuration } from 'types/Configuration'
interface TabPanelProps {
  children?: React.ReactNode
  index: number
  value: number
}

const CustomTabPanel = (props: TabPanelProps) => {
  const { children, value, index, ...other } = props
  return (
    <Box
      component={'div'}
      role="tabpanel"
      hidden={value !== index}
      id={`dashboard-tabpanel-${index}`}
      aria-labelledby={`dashboard-tab-${index}`}
      {...other}
    >
      {value === index && children}
    </Box>
  )
}

const tabProps = (index: number) => {
  return {
    id: `dashboard-tab-${index}`,
    'aria-controls': `dashboard-tabpanel-${index}`
  }
}

const Dashboard = () => {
  const dashboardData = useDashboardData()
  const [currentTabIndex, setCurrentTabIndex] = useState(0)
  const handleChangeTab = (event: React.SyntheticEvent, newValue: number) => {
    setCurrentTabIndex(newValue)
  }
  return (
    <Container maxWidth={false}>
      <Stack
        padding={{
          lg: '0rem 1rem 1rem 1rem',
          xs: '1rem 0rem 0rem 0rem',
          backgroundColor: '#fff',
          borderRadius: '1rem'
        }}
        minHeight={'80vh'}
      >
        <Tabs
          value={currentTabIndex}
          onChange={handleChangeTab}
          sx={{
            paddingY: { lg: '2rem', xs: ' 0.5rem' }
          }}
          aria-label="dashboard tabs"
          variant="scrollable"
          scrollButtons
          allowScrollButtonsMobile
        >
          <Tab
            label={<Typography variant="h5">Confusion Matrix</Typography>}
            {...tabProps(0)}
          />
          <Tab
            label={<Typography variant="h5">M & U Values</Typography>}
            {...tabProps(1)}
          />
          <Tab
            label={<Typography variant="h5"> Import Process Status</Typography>}
            {...tabProps(2)}
          />
        </Tabs>
        <Box padding={'1rem 1rem 1rem 1rem'}>
          <CustomTabPanel value={currentTabIndex} index={0}>
            <Grid container spacing={{ xs: 2, md: 5 }}>
              <Grid item xs={12} lg={6}>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Box component="fieldset">
                      <legend>Records</legend>
                      <Grid container spacing={2}>
                        <Grid item xs={12} lg={6}>
                          <CountWidget
                            label="Golden Record"
                            value={
                              /* TODO: Improve */ dashboardData.isReady
                                ? dashboardData?.data?.dashboardData
                                    ?.dashboardData?.linker_stats
                                    ?.goldenRecordCount
                                : 0
                            }
                            icon={<Person sx={{ fontSize: '3.5rem' }} />}
                            iconBackgroundColor={'#FFD700'}
                          />
                        </Grid>
                        <Grid item xs={12} lg={6}>
                          <CountWidget
                            label="Interactions "
                            value={
                              /* TODO: Improve */ dashboardData.isReady
                                ? dashboardData?.data?.dashboardData
                                    ?.dashboardData?.linker_stats
                                    ?.interactionsCount
                                : 0
                            }
                            icon={<Layers sx={{ fontSize: '3.5rem' }} />}
                            iconBackgroundColor={'primary.main'}
                          />
                        </Grid>
                      </Grid>
                    </Box>
                  </Grid>
                  <Grid item xs={12}>
                    <Box component="fieldset">
                      <legend> Notifications </legend>
                      <Grid container spacing={2}>
                        <Grid item xs={12} lg={6}>
                          <CountWidget
                            label="New/Open"
                            value={
                              /* TODO: Improve */ dashboardData.isReady
                                ? dashboardData.data?.sqlDashboardData
                                    ?.dashboardData?.notificationStats
                                    ?.openNotifications
                                : 0
                            }
                            icon={
                              <NotificationAdd sx={{ fontSize: '3.5rem' }} />
                            }
                            iconBackgroundColor={'#76ff03'}
                          />
                        </Grid>
                        <Grid item xs={12} lg={6}>
                          <CountWidget
                            label="Closed"
                            value={
                              /* TODO: Improve */ dashboardData.isReady
                                ? dashboardData.data?.sqlDashboardData
                                    ?.dashboardData?.notificationStats
                                    ?.closedNotifications
                                : 0
                            }
                            icon={
                              <NotificationsOff sx={{ fontSize: '3.5rem' }} />
                            }
                            iconBackgroundColor={pink[600]}
                          />
                        </Grid>
                      </Grid>
                    </Box>
                  </Grid>
                  <Grid item xs={12}>
                    <BetaFscore
                      data={
                        /* TODO: Improve */ dashboardData.isReady
                          ? dashboardData?.data?.dashboardData?.dashboardData
                              ?.tptn?.tptnfScore
                          : null
                      }
                    />
                  </Grid>
                </Grid>
              </Grid>
              <Grid item xs={12} lg={6}>
                <Box component="fieldset" minHeight={'550px'}>
                  <legend>Confusion Matrix</legend>
                  <ConfusionMatrix
                    data={
                      /* TODO: Improve */ dashboardData.isReady
                        ? dashboardData?.data?.dashboardData?.dashboardData
                            ?.tptn
                        : null
                    }
                  />
                </Box>
              </Grid>
            </Grid>
          </CustomTabPanel>
          <CustomTabPanel value={currentTabIndex} index={1}>
            <Grid container sx={{ minHeight: { lg: '450px' } }}>
              <Grid item xs={6}>
                <MandU
                  data={
                    /* TODO: Improve */ dashboardData.isReady
                      ? dashboardData?.data?.dashboardData?.dashboardData
                          ?.m_and_u
                      : null
                  }
                />
              </Grid>
            </Grid>
          </CustomTabPanel>
          <CustomTabPanel value={currentTabIndex} index={2}>
            <ImportProcessWidget
              data={
                /* TODO: Improve */ dashboardData.isReady
                  ? dashboardData?.data?.dashboardData?.dashboardData
                      ?.linker_stats?.linkerProgressStats
                  : null
              }
            />
          </CustomTabPanel>
        </Box>
      </Stack>
    </Container>
  )
}

export default Dashboard
