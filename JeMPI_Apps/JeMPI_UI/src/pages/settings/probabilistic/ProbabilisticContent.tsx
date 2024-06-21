import { Typography, Grid, TextField, Slider, Button } from '@mui/material'
import { Box } from '@mui/system'
import { useFormik } from 'formik'
import { defaultValues, initializeValues, marks, validationSchema } from './ProbabilisticConstants'
import { useConfiguration } from 'hooks/useUIConfiguration'
import { Configuration } from 'types/Configuration'

interface Rule {
  vars: string[]
  text: string
  linkThreshold?: number
  marginWindowSize?: number
  doNotLinkWindow?: { low: number; high: number }
  reviewThresholdRange?: { low: number; high: number }
}

interface ProbabilisticContentProps {
  linkingRules: {
    link?: { probabilistic?: Rule[] }
    validate?: { probabilistic?: Rule[] }
    matchNotification?: { probabilistic?: Rule[] }
  }
  currentTab: 'link' | 'validate' | 'matchNotification'

}

const ProbabilisticContent = ({
  linkingRules = {},
  currentTab
}: ProbabilisticContentProps) => {
  const { configuration, setConfiguration } = useConfiguration()

  const probabilisticRules = [
    ...(linkingRules.link?.probabilistic || []),
    ...(linkingRules.validate?.probabilistic || []),
    ...(linkingRules.matchNotification?.probabilistic || [])
  ]

  const rule = probabilisticRules[0] || {}
  const initialValues = initializeValues(rule);
 
  const { handleChange, handleSubmit, values, setFieldValue, errors, touched } =
    useFormik({
      initialValues,
      validationSchema,
      onSubmit: values => {
        const finalValues = {
          minReviewThreshold:
            parseFloat(values.minReviewThreshold) ||
            parseFloat(defaultValues.minReviewThreshold),
          linkThreshold:
            parseFloat(values.linkThreshold) ||
            parseFloat(defaultValues.linkThreshold),
          maxReviewThreshold:
            parseFloat(values.maxReviewThreshold) ||
            parseFloat(defaultValues.maxReviewThreshold),
          marginWindowSize:
            parseFloat(values.marginWindowSize) ||
            parseFloat(defaultValues.marginWindowSize)
        }

        const handleUpdateConfiguration = (tab: 'link' | 'validate' | 'matchNotification') => {
          setConfiguration(prevConfig => {
            if (!prevConfig) return prevConfig

            const updatedConfiguration: Configuration = {
              ...prevConfig,
              rules: {
                ...prevConfig.rules,
                [tab]: {
                  ...prevConfig.rules[tab],
                  probabilistic: [
                    {
                      ...rule,
                      linkThreshold: finalValues.linkThreshold,
                      reviewThresholdRange: {
                        low: finalValues.minReviewThreshold,
                        high: finalValues.maxReviewThreshold
                      },
                      marginWindowSize: finalValues.marginWindowSize
                    }
                  ]
                }
              }
            }
            return updatedConfiguration
          })
        }

        handleUpdateConfiguration(currentTab)
        localStorage.setItem('configuration', JSON.stringify(configuration))
      }
    })

   
const handleValidatedChange = (field: string, value: string) => {
  const parsedValue = parseFloat(value);
  const adjustmentFactor = 0.1;
  const roundToTwoDecimals = (num: number) => parseFloat(num.toFixed(2));

  if (field === 'linkThreshold') {
    if (parsedValue >= parseFloat(values.minReviewThreshold) && parsedValue <= parseFloat(values.maxReviewThreshold)) {
      setFieldValue('linkThreshold', value);
    } else {
      const newMinThreshold = roundToTwoDecimals(Math.max(0, parsedValue - adjustmentFactor));
      const newMaxThreshold = roundToTwoDecimals(Math.min(1, parsedValue + adjustmentFactor));
      const newDoNotLinkWindowStart = roundToTwoDecimals(Math.max(0, newMinThreshold - adjustmentFactor));
      const newDoNotLinkWindowEnd = roundToTwoDecimals(Math.min(1, newMaxThreshold + adjustmentFactor));
      setFieldValue('linkThreshold', value);
      setFieldValue('minThreshold', newMinThreshold.toString());
      setFieldValue('maxThreshold', newMaxThreshold.toString());
      setFieldValue('doNotLinkWindowStart', newDoNotLinkWindowStart.toString());
      setFieldValue('doNotLinkWindowEnd', newDoNotLinkWindowEnd.toString());
    }

  } 

};
    
    
  

  return (
    <Box
      sx={{
        height: '55vh',
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '20px'
      }}
    >
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          width: '100%',
          padding: '20px'
        }}
      >
        <form onSubmit={handleSubmit}>
          <Grid
            container
            spacing={2}
            sx={{ marginBottom: '80px', alignItems: 'center' }}
          >
        <Slider
        id="slider-summary"
        getAriaValueText={e => e.toString()}
        valueLabelDisplay="auto"
        step={0.05}
        marks={marks}
        aria-labelledby="slider-summary"
        min={0}
        max={1}
        value={[
          parseFloat(values.minReviewThreshold),
          parseFloat(values.linkThreshold),
          parseFloat(values.maxReviewThreshold),
        ]}
        sx={{
          width: '50%',
          '& .MuiSlider-thumb': {
            "&[data-index='1']": { backgroundColor: 'red' },
            "&[data-index='2']": { backgroundColor: 'green' },
            "&[data-index='3']": { backgroundColor: 'red' },
          },
          marginBottom: '80px'
        }}
        track={false}
      />
            <Grid item xs={8}>
              <Grid container spacing={2} sx={{ alignItems: 'center' }}>
                <Grid item xs={4}>
                  <Typography sx={{ color: 'green' }}>
                    Link Threshold
                  </Typography>
                </Grid>
                <Grid item xs={4}>
                  <TextField
                    id="link-threshold"
                    name="linkThreshold"
                    label="Enter floating point"
                    variant="outlined"
                    size="small"
                    value={values.linkThreshold}
                    onChange={e =>
                      handleValidatedChange('linkThreshold', e.target.value)
                    }
                    error={
                      touched.linkThreshold && Boolean(errors.linkThreshold)
                    }
                    helperText={touched.linkThreshold && errors.linkThreshold}
                    inputProps={{ min: 0, max: 1 }}
                  />
                </Grid>
              </Grid>
              <Grid
                container
                spacing={2}
                sx={{ alignItems: 'center', marginTop: '10px' }}
              >
                <Grid item xs={4}>
                  <Typography sx={{ color: 'red' }}>
                    Review Threshold range
                  </Typography>
                </Grid>
                <Grid item xs={4}>
                  <TextField
                    id="min-review-threshold"
                    name="min-review-threshold"
                    label="Min"
                    variant="outlined"
                    size="small"
                    value={values.minReviewThreshold}
                    onChange={e =>
                      handleValidatedChange('minThreshold', e.target.value)
                    }
                    error={touched.minReviewThreshold && Boolean(errors.minReviewThreshold)}
                    helperText={touched.minReviewThreshold && errors.minReviewThreshold}
                    inputProps={{ min: 0, max: 1 }}
                  />
                </Grid>
              </Grid>
              <Grid
                container
                spacing={2}
                sx={{ alignItems: 'center', marginTop: '10px' }}
              >
                <Grid item xs={4}>
                  <Typography sx={{ color: 'blue' }}>
                    Max Review Threshold
                  </Typography>
                </Grid>
                <Grid item xs={4}>
                  <TextField
                    id="max-review-threshold"
                    name="max-review-threshold"
                    label="Start"
                    variant="outlined"
                    size="small"
                    value={values.maxReviewThreshold}
                    onChange={e =>
                      handleValidatedChange(
                        'max-review-threshold',
                        e.target.value
                      )
                    }
                    error={
                      touched.maxReviewThreshold &&
                      Boolean(errors.maxReviewThreshold)
                    }
                    helperText={
                      touched.maxReviewThreshold &&
                      errors.maxReviewThreshold
                    }
                    inputProps={{ min: 0, max: 1 }}
                  />
                </Grid>
              
              </Grid>
              <Grid
                container
                spacing={2}
                sx={{ alignItems: 'center', marginTop: '10px' }}
              >
                <Grid item xs={4}>
                  <Typography>Margin Window Size</Typography>
                </Grid>
                <Grid item xs={4}>
                  <TextField
                    id="margin-window-size"
                    name="marginWindowSize"
                    label="Enter floating point"
                    variant="outlined"
                    size="small"
                    value={values.marginWindowSize}
                    onChange={handleChange}
                    error={
                      touched.marginWindowSize && Boolean(errors.marginWindowSize)
                    }
                    helperText={
                      touched.marginWindowSize && errors.marginWindowSize
                    }
                  />
                </Grid>
              </Grid>
            </Grid>
         
          </Grid>

          <Box sx={{ marginTop: '20px' }}>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={
                Object.values(errors).some(error => error !== undefined)
              }
            >
              Save
            </Button>
          </Box>
        </form>
      </Box>
    </Box>
  )
}

export default ProbabilisticContent
