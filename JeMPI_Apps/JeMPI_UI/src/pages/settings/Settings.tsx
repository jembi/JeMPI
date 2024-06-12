import { Grid, Tab, Tabs, Typography } from '@mui/material'
import { Box } from '@mui/system'
import { SyntheticEvent, useMemo, useState } from 'react'
import CommonSettings from './common/Common'
import UniqueToGR from './uniqueToGR/UniqueToGR'
import UniqueToInteraction from './uniqueToInteraction/UniqueToInteraction'
import Deterministic from './deterministic/Deterministic'
import Blocking from './blocking/Blocking'
import GoldenRecordLists from './goldenRecordLists/GoldenRecordLists'
import { useConfig } from 'hooks/useConfig'
import { useQuery } from '@tanstack/react-query'
import { generateId } from 'utils/helpers'
import Loading from 'components/common/Loading'
import InteractiveNode from './interactiveNode/InteractiveNode'
import { Configuration } from 'types/Configuration'
import { CustomTabPanel, a11yProps } from './deterministic/BasicTabs'

const Settings = () => {
  const [value, setValue] = useState(0)
  const { apiClient } = useConfig()

  const { data: fields, isLoading } = useQuery<Configuration>({
    queryKey: ['configuration'],
    queryFn: () => apiClient.fetchConfiguration(),
    refetchOnWindowFocus: false
  })

  const handleChange = (event: SyntheticEvent, newValue: number) => {
    setValue(newValue)
  }

  const configuration = useMemo(() => {
    return fields ? generateId(fields) : ({} as Configuration)
  }, [fields])

  if (isLoading) {
    return <Loading />
  }

  return (
    <Grid container spacing={4}>
      <Grid item md={4}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            justifyContent: 'center',
            alignItems: 'center',
            justifyItems: 'center',
            alignContent: 'center',
            gap: 0,
            width: '100%'
          }}
        >
          <div className="shapes-container">
            <InteractiveNode />
          </div>
        </div>
      </Grid>
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
              <Tab label="Unique to Interaction" {...a11yProps(2)} />
              <Tab label="Golden Records Lists" {...a11yProps(3)} />
              <Tab label="Deterministic" {...a11yProps(4)} />
              <Tab label="Blocking" {...a11yProps(5)} />
              <Tab label="Probabilistic" {...a11yProps(6)} />
            </Tabs>
          </Box>
          <CustomTabPanel value={value} index={0}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup common properties
            </Typography>
            <CommonSettings demographicData={configuration.demographicFields} />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties that are unique to the golden record
            </Typography>
            <UniqueToGR
              uniqueToGoldenRecordData={configuration.auxGoldenRecordFields}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties that are unique to the interaction
            </Typography>
            <UniqueToInteraction
              uniqueInteractionData={configuration.auxInteractionFields}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={3}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties for Golden record lists
            </Typography>
            <GoldenRecordLists goldenRecordList={fields?.additionalNodes} />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={4}>
            <Deterministic
              rules={configuration.rules}
              demographicData={fields?.demographicFields || []}
            />
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