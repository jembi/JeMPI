import { Grid, Tab, Tabs, Typography, useMediaQuery } from '@mui/material'
import { Box } from '@mui/system'
import React, { SyntheticEvent, useState } from 'react'
import CommonSettings from './common/Common'
import UniqueToGR from './uniqueToGR/UniqueToGR'
import UniqueToInteraction from './uniqueToInteraction/UniqueToInteraction'
import Deterministic from './deterministic/deterministic'
import Blocking from './blocking/Blocking'
import GoldenRecordLists from './goldenRecordLists/GoldenRecordLists'

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
      id={`settings-tabpanel-${index}`}
      aria-labelledby={`settings-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3, backgroundColor: '#f5f5f5' }}>{children}</Box>
      )}
    </div>
  )
}

function a11yProps(index: number) {
  return {
    id: `settings-tab-${index}`,
    'aria-controls': `settings-tabpanel-${index}`
  }
}
const Settings = () => {
  const [value, setValue] = useState(0)
  const handleChange = (event: SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }

  return (
    <Grid container spacing={4}>
      <Grid item md={4}></Grid>
      <Grid item md={8}>
        <Box sx={{ width: '100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs
              value={value}
              onChange={handleChange}
              aria-label=" tabs "
              variant="scrollable"
            >
              <Tab label="Common" {...a11yProps(0)} />
              <Tab label="Unique to Golden record" {...a11yProps(1)} />
              <Tab label=" Unique to Interaction" {...a11yProps(2)} />
              <Tab label="Golden Records Lists" {...a11yProps(3)} />
              <Tab label="Deterministic" {...a11yProps(4)} />
              <Tab label=" Blocking" {...a11yProps(5)} />
              <Tab label="Probabilistic" {...a11yProps(6)} />
            </Tabs>
          </Box>
          <CustomTabPanel value={value} index={0}>
            <>
              <Typography variant="h5" sx={{ py: 3 }}>
                Setup common properties
              </Typography>
              <CommonSettings />
            </>
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Unique to Golden record
            </Typography>
            <UniqueToGR />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Unique to Interaction
            </Typography>
            <UniqueToInteraction />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={3}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties for Golden Records Lists{' '}
            </Typography>
            <GoldenRecordLists />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={4}>
            Deterministic
            <Deterministic />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={5}>
            Blocking
            <Blocking />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={6}>
            Probabilistic
          </CustomTabPanel>
        </Box>
      </Grid>
    </Grid>
  )
}

export default Settings
