import { Card, CardContent, Tab, Tabs } from '@mui/material'
import { Box } from '@mui/system'
import React, {useState } from 'react'
import { a11yProps, CustomTabPanel } from '../deterministic/BasicTabs'
import BlockingContent from './BlockingContent'
import { useConfiguration } from 'hooks/useUIConfiguration'


const Blocking = () => {
  const [value, setValue] = useState(0)
  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }
  const { configuration,} = useConfiguration()

  const matchNotificationRules =
    configuration?.rules.matchNotification?.probabilistic ?? []
  const linkingRules = configuration?.rules.link?.probabilistic ?? []

  return (
    <Card sx={{ minWidth: 275 }}>
      <CardContent
        sx={{
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: '#f5f5f5'
        }}
      >
        <Box sx={{ width: '100%' }}>
          <Tabs
            value={value}
            onChange={handleChange}
            aria-label="basic tabs example"
          >
            <Tab label="Linking" {...a11yProps(0)} />
            <Tab label="Matching" {...a11yProps(1)} />
          </Tabs>
          <CustomTabPanel value={value} index={0}>
            <BlockingContent
              hasUndefinedRule={linkingRules.length === 0}
              linkingRules={{ link: { probabilistic: linkingRules } }}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <BlockingContent
              hasUndefinedRule={matchNotificationRules.length === 0}
              linkingRules={{
                matchNotification: { probabilistic: matchNotificationRules }
              }}
            />
          </CustomTabPanel>
        </Box>
      </CardContent>
    </Card>
  )
}

export default Blocking
