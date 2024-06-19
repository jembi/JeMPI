import { useEffect, useState } from 'react'
import { Card, CardContent, Tab, Tabs } from '@mui/material'
import { Box } from '@mui/system'
import { a11yProps, CustomTabPanel } from '../deterministic/BasicTabs'
import ProbabilisticContent from './ProbabilisticContent'
import { Rule } from 'types/Configuration'

interface ProbabilisticProps {
  rules: {
    link?: {
      probabilistic?: Rule[]
    }
    validate?: {
      probabilistic?: Rule[]
    }
    matchNotification?: {
      probabilistic?: Rule[]
    }
  }
}

const Probabilistic = ({ rules = {} }: ProbabilisticProps) => {
  const [value, setValue] = useState(0)

  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }

  useEffect(() => {
    console.log('rules', rules)
  }, [])

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
            <ProbabilisticContent
              linkingRules={{
                link: { probabilistic: rules.link?.probabilistic ?? [] }
              }}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <ProbabilisticContent
              linkingRules={{
                validate: { probabilistic: rules.validate?.probabilistic ?? [] }
              }}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <ProbabilisticContent
              linkingRules={{
                matchNotification: {
                  probabilistic: rules.matchNotification?.probabilistic ?? []
                }
              }}
            />
          </CustomTabPanel>
        </Box>
      </CardContent>
    </Card>
  )
}

export default Probabilistic
