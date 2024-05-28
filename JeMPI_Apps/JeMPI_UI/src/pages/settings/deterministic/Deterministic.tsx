import {
  Box,
  Card,
  CardContent,
  Tab,
  Tabs
} from '@mui/material'
import { useState } from 'react'
import { DemographicField, Configuration } from 'types/Configuration'
import { CustomTabPanel, a11yProps } from './BasicTabs'
import DeterministicContent from './DeterministicContent'

interface DeterministicProps {
  demographicData: DemographicField[]
  linkingRules: Configuration['rules']['link']
}

const Deterministic = ({
  demographicData = [],
  linkingRules
}: DeterministicProps) => {
  const [value, setValue] = useState(0);

  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue);
  };

  return (
    <Card sx={{ minWidth: 275 }}>
      <CardContent
        sx={{
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          gap: 6
        }}
      >
        <Box sx={{ width: '100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
              <Tab label="Validate" {...a11yProps(0)} />
              <Tab label="Linking" {...a11yProps(1)} />
              <Tab label="Matching" {...a11yProps(2)} />
            </Tabs>
          </Box>
          <CustomTabPanel value={value} index={0}>
            <DeterministicContent demographicData={demographicData} linkingRules={linkingRules} />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <DeterministicContent demographicData={demographicData} linkingRules={linkingRules} />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <DeterministicContent demographicData={demographicData} linkingRules={linkingRules} />
          </CustomTabPanel>
        </Box>
      </CardContent>
    </Card>
  )
}

export default Deterministic
