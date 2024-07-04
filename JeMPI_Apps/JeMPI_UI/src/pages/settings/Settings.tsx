import { useEffect, useState } from 'react';
import { Grid, Tab, Tabs, Typography } from '@mui/material';
import { Box } from '@mui/system';
import { SyntheticEvent } from 'react';
import CommonSettings from './common/Common';
import UniqueToGR from './uniqueToGR/UniqueToGR';
import UniqueToInteraction from './uniqueToInteraction/UniqueToInteraction';
import Deterministic from './deterministic/Deterministic';
import Blocking from './blocking/Blocking';
import GoldenRecordLists from './goldenRecordLists/GoldenRecordLists';
import InteractiveNode from './interactiveNode/InteractiveNode';
import { CustomTabPanel, a11yProps } from './deterministic/BasicTabs';
import { Configuration } from 'types/Configuration';
import { generateId } from 'utils/helpers';

const Settings = () => {
  const [value, setValue] = useState(0);
  const [configurationData, setConfigurationData] = useState(() => {
    const storedData = localStorage.getItem('configuration');
    return storedData ? generateId(JSON.parse(storedData)) : ({} as Configuration);
  });

  const handleChange = (event: SyntheticEvent, newValue: number) => {
    setValue(newValue);
  };

  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'configuration') {
        const newConfigData = localStorage.getItem('configuration');
        setConfigurationData(newConfigData ? generateId(JSON.parse(newConfigData)) : ({} as Configuration));
      }
    };

    window.addEventListener('storage', handleStorageChange);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  useEffect(() => {
    const storedData = localStorage.getItem('configuration');
    if (storedData) {
      setConfigurationData(generateId(JSON.parse(storedData)));
    }
  }, []);

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
              aria-label="tabs"
              variant="scrollable"
            >
              <Tab label="Common" {...a11yProps(0)} />
              <Tab label="Unique to Golden Record" {...a11yProps(1)} />
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
            <CommonSettings />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties that are unique to the golden record
            </Typography>
            <UniqueToGR
              uniqueToGoldenRecordData={configurationData?.auxGoldenRecordFields || []}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties that are unique to the interaction
            </Typography>
            <UniqueToInteraction
              uniqueInteractionData={configurationData?.auxInteractionFields || []}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={3}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Setup properties for Golden record lists
            </Typography>
            <GoldenRecordLists goldenRecordList={[]}/>
          </CustomTabPanel>
          <CustomTabPanel value={value} index={4}>
            <Deterministic
              rules={configurationData.rules}
              demographicData={configurationData.demographicFields || []}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={5}>
            <Blocking
              demographicData={configurationData.demographicFields}
              rules={configurationData?.rules || {}}
            />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={6}>
            <Typography variant="h5" sx={{ py: 3 }}>
              Probabilistic
            </Typography>
          </CustomTabPanel>
        </Box>
      </Grid>
    </Grid>
  );
};

export default Settings;
