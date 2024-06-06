import { Box, Card, CardContent, Tab, Tabs } from '@mui/material'
import { useState } from 'react'
import { Field, Rule } from 'types/Configuration'
import { CustomTabPanel, a11yProps } from './BasicTabs'
import DeterministicContent from './DeterministicContent'

interface DeterministicProps {
  demographicData: Field[]
  rules: {
    link?: {
      deterministic?: Rule[]
    }
    validate?: {
      deterministic?: Rule[]
    }
    matchNotification?: {
      deterministic?: Rule[]
    }
  }
}

const Deterministic = ({
  demographicData = [],
  rules = {}
}: DeterministicProps) => {
  const [value, setValue] = useState(0)

  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }

  const linkingRules = rules.link?.deterministic ?? []
  const validateRules = rules.validate?.deterministic ?? []
  const matchNotificationRules = rules.matchNotification?.deterministic ?? []

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
              demographicData={demographicData}
              linkingRules={{ link: { deterministic: linkingRules } }}
              hasUndefinedRule={linkingRules.length === 0}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={{ link: { deterministic: validateRules } }}
              hasUndefinedRule={validateRules.length === 0}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={{ link: { deterministic: matchNotificationRules } }}
              hasUndefinedRule={matchNotificationRules.length === 0}
            />
          </CustomTabPanel>
        </Box>
      </CardContent>
    </Card>
  )
}

export default Deterministic
