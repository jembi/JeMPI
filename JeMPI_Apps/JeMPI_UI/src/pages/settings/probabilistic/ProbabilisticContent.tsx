import { Typography, Grid, TextField, Slider } from '@mui/material'
import { Box } from '@mui/system'
import { Form, Formik } from 'formik'
import {
  defaultValues,
  initializeValues,
  marks
} from './ProbabilisticConstants'
import { useConfiguration } from 'hooks/useUIConfiguration'
import { Configuration } from 'types/Configuration'
import { LoadingButton } from '@mui/lab'

interface Rule {
  vars: string[]
  text: string
  linkThreshold?: number
  marginWindowSize?: number
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
  const { configuration, setConfiguration } = useConfiguration();

  const probabilisticRules = [
    ...(linkingRules.link?.probabilistic || []),
    ...(linkingRules.validate?.probabilistic || []),
    ...(linkingRules.matchNotification?.probabilistic || [])
  ]

  const rule = probabilisticRules[0] || {}
  const initialValues = initializeValues(rule)
  return (
    <Formik
      initialValues={initialValues}
      onSubmit={async values => {
        const finalValues = {
          minReviewThreshold:
            parseFloat(values.minReviewThreshold) ||
            defaultValues.minReviewThreshold,
          linkThreshold:
            parseFloat(values.linkThreshold) || defaultValues.linkThreshold,
          maxReviewThreshold:
            parseFloat(values.maxReviewThreshold) ||
            defaultValues.maxReviewThreshold,
          marginWindowSize:
            parseFloat(values.marginWindowSize) ||
            defaultValues.marginWindowSize
        }

        const handleUpdateConfiguration = (
          tab: 'link' | 'validate' | 'matchNotification'
        ) => {
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
            if (!updatedConfiguration.rules[tab].probabilistic) {
              updatedConfiguration.rules[tab].probabilistic = []
            }

            return updatedConfiguration
          })
        }

        handleUpdateConfiguration(currentTab)
        localStorage.setItem('configuration', JSON.stringify(configuration))
      }}
    >
      {({
        values,
        handleChange,
        setFieldValue,
        handleSubmit,
        isSubmitting,
        touched,
        errors
      }) => (
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
            <Form onSubmit={handleSubmit}>
              <Grid
                container
                spacing={2}
                sx={{ marginBottom: '80px', alignItems: 'center' }}
              >
                <Slider
                  id="slider-summary"
                  valueLabelDisplay="auto"
                  step={0.05}
                  marks={marks}
                  min={0}
                  max={1}
                  value={[
                    parseFloat(values.minReviewThreshold),
                    parseFloat(values.linkThreshold),
                    parseFloat(values.maxReviewThreshold)
                  ]}
                  onChange={(e, value) => {
                    if (Array.isArray(value)) {
                      setFieldValue('minReviewThreshold', value[0].toString())
                      setFieldValue('linkThreshold', value[1].toString())
                      setFieldValue('maxReviewThreshold', value[2].toString())
                    } else {
                      setFieldValue('linkThreshold', value.toString())
                    }
                  }}
                  sx={{
                    width: '50%',
                    '& .MuiSlider-thumb': {
                      "&[data-index='0']": { backgroundColor: 'red' },
                      "&[data-index='1']": { backgroundColor: 'green' },
                      "&[data-index='2']": { backgroundColor: 'blue' }
                    },
                    marginBottom: '80px'
                  }}
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
                        onChange={handleChange}
                        error={
                          touched.linkThreshold && Boolean(errors.linkThreshold)
                        }
                        helperText={
                          touched.linkThreshold && errors.linkThreshold
                        }
                        inputProps={{ min: 0.2, max: 0.95, step: 0.01 }}
                        InputLabelProps={{ style: { color: 'green' } }}
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
                        name="minReviewThreshold"
                        label="Min"
                        variant="outlined"
                        size="small"
                        value={values.minReviewThreshold}
                        onChange={e => {
                          if (
                            +e.target.value < parseFloat(values.linkThreshold)
                          ) {
                            handleChange(e)
                          }
                        }}
                        error={
                          touched.minReviewThreshold &&
                          Boolean(errors.minReviewThreshold)
                        }
                        helperText={
                          touched.minReviewThreshold &&
                          errors.minReviewThreshold
                        }
                        inputProps={{
                          min: 0.19,
                          max: values.linkThreshold,
                          step: 0.01
                        }}
                        InputLabelProps={{ style: { color: 'red' } }}
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
                        name="maxReviewThreshold"
                        label="max"
                        variant="outlined"
                        size="small"
                        value={values.maxReviewThreshold}
                        onChange={e => {
                          if (
                            +e.target.value > parseFloat(values.linkThreshold)
                          ) {
                            handleChange(e)
                          }
                        }}
                        inputProps={{
                          min: values.linkThreshold,
                          max: 0.96,
                          step: 0.01
                        }}
                        InputLabelProps={{ style: { color: '#1976D2' } }}
                        error={
                          touched.maxReviewThreshold &&
                          Boolean(errors.maxReviewThreshold)
                        }
                        helperText={
                          touched.maxReviewThreshold &&
                          errors.maxReviewThreshold
                        }
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
                          touched.marginWindowSize &&
                          Boolean(errors.marginWindowSize)
                        }
                        helperText={
                          touched.marginWindowSize && errors.marginWindowSize
                        }
                        inputProps={{ min: 0, max: 0.2, step: 0.01 }}
                      />
                    </Grid>
                  </Grid>
                </Grid>
              </Grid>

              <Box sx={{ marginTop: '20px' }}>
                <LoadingButton
                  type="submit"
                  variant="contained"
                  color="primary"
                  loadingPosition="start"
                  disabled={isSubmitting}
                >
                  Save
                  </LoadingButton>
              </Box>
            </Form>
          </Box>
        </Box>
      )}
    </Formik>
  )
}

export default ProbabilisticContent
