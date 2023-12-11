import { Grid, Tab, Tabs, Typography } from '@mui/material'
import { Box } from '@mui/system'
import React, { SyntheticEvent, useState } from 'react'

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
      {value === index && (
        <Box sx={{ p: 3 }}>
          <Typography>{children}</Typography>
        </Box>
      )}
    </div>
  )
}

function a11yProps(index: number) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`
  }
}
const Settings = () => {
  const [value, setValue] = useState(0)

  const handleChange = (event: SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }

  return (
    <Grid container spacing={2}>
      <Grid item xs={2}></Grid>
      <Grid item xs={10}>
        <Box sx={{ width: '100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs
              value={value}
              onChange={handleChange}
              aria-label="basic tabs example"
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
            Common
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            Unique to Golden record
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            Unique to Interaction
          </CustomTabPanel>
          <CustomTabPanel value={value} index={3}>
            Golden Records Lists
          </CustomTabPanel>
          <CustomTabPanel value={value} index={4}>
            Deterministic
          </CustomTabPanel>
          <CustomTabPanel value={value} index={5}>
            Blocking
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
