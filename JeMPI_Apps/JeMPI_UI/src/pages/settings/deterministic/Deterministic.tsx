import { Box, Card, CardContent, Tab, Tabs } from '@mui/material'
import { useState } from 'react'
import { CustomTabPanel, a11yProps } from './BasicTabs'
import DeterministicContent from './DeterministicContent'
import { useConfiguration } from 'hooks/useUIConfiguration'

const Deterministic = () => {
  const [value, setValue] = useState(0)
  const {configuration} = useConfiguration()

  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }

  const linkingRules = configuration?.rules.link?.deterministic ?? []
  const validateRules = configuration?.rules.validate?.deterministic ?? []
  const matchNotificationRules = configuration?.rules.matchNotification?.deterministic ?? []

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
        <Box sx={{ width: '100%', backgroundColor: '#f5f5f5' }}>
          <Box>
            <Tabs
              value={value}
              onChange={handleChange}
              aria-label="basic tabs example"
            >
              <Tab label="Linking" {...a11yProps(0)} />
              <Tab label="Validate" {...a11yProps(1)} />
              <Tab label="Matching" {...a11yProps(2)} />
            </Tabs>
          </Box>
          <CustomTabPanel value={value} index={0}>
            <DeterministicContent
              demographicData={configuration?.demographicFields || []}
              linkingRules={{ link: { deterministic: linkingRules } }}
              hasUndefinedRule={linkingRules.length === 0}
              currentTab= {'link'}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <DeterministicContent
              demographicData={configuration?.demographicFields || []}
              linkingRules={{ link: { deterministic: validateRules } }}
              hasUndefinedRule={validateRules.length === 0}
              currentTab= {'validate'}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <DeterministicContent
              demographicData={configuration?.demographicFields || []}
              linkingRules={{ link: { deterministic: matchNotificationRules } }}
              hasUndefinedRule={matchNotificationRules.length === 0}
              currentTab = {'matchNotification'}
              
            />
          </CustomTabPanel>
        </Box>
      </CardContent>
    </Card>
  )
}

export default Deterministic
