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
import { useEffect, useState } from 'react'
import { useSnackbar } from 'notistack'


interface Rule {
  vars: string[]
  text: string
  linkThreshold?: number
  marginWindowSize?: number
  reviewThresholdRange?: { low: number; high: number }
}

const Probabilistic = () => {
  const { configuration, setConfiguration } = useConfiguration()
  const [rule, setRule] = useState<Rule>({
    vars: [],
    text: '',
    linkThreshold: defaultValues.linkThreshold,
    marginWindowSize: defaultValues.marginWindowSize,
    reviewThresholdRange: {
      low: defaultValues.minReviewThreshold,
      high: defaultValues.maxReviewThreshold
    }
  })
  const { enqueueSnackbar } = useSnackbar()

  useEffect(() => {
    if (configuration) {
      const probabilisticRules = configuration.rules.link.probabilistic || []
      setRule(
        probabilisticRules[0] || {
          vars: [],
          text: '',
          linkThreshold: defaultValues.linkThreshold,
          marginWindowSize: defaultValues.marginWindowSize,
          reviewThresholdRange: {
            low: defaultValues.minReviewThreshold,
            high: defaultValues.maxReviewThreshold
          }
        }
      )
    }
  }, [configuration])

  const handleUpdateConfiguration = (values: any) => {
    if (!configuration) return

    const updatedConfiguration: Configuration = {
      ...configuration,
      rules: {
        ...configuration.rules,
        link: {
          ...configuration.rules.link,
          probabilistic: [
            {
              ...rule,
              linkThreshold: parseFloat(values.linkThreshold),
              reviewThresholdRange: {
                low: parseFloat(values.minReviewThreshold),
                high: parseFloat(values.maxReviewThreshold)
              },
              marginWindowSize: parseFloat(values.marginWindowSize)
            }
          ]
        }
      }
    }

    setConfiguration(updatedConfiguration)
    localStorage.setItem('configuration', JSON.stringify(updatedConfiguration))
    enqueueSnackbar(`Successfully saved probabilistic rule`, {
      variant: 'success'
    })
  }

  const initialValues = initializeValues(rule)

  return (
    <Formik
      enableReinitialize
      initialValues={initialValues}
      onSubmit={async values => {
        const finalValues = {
          minReviewThreshold:
            parseFloat(values.minReviewThreshold.toString()) ||
            defaultValues.minReviewThreshold,
          linkThreshold:
            parseFloat(values.linkThreshold.toString()) ||
            defaultValues.linkThreshold,
          maxReviewThreshold:
            parseFloat(values.maxReviewThreshold.toString()) ||
            defaultValues.maxReviewThreshold,
          marginWindowSize:
            parseFloat(values.marginWindowSize.toString()) ||
            defaultValues.marginWindowSize
        }

        handleUpdateConfiguration(finalValues)
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
                    parseFloat(values.minReviewThreshold.toString()),
                    parseFloat(values.linkThreshold.toString()),
                    parseFloat(values.maxReviewThreshold.toString())
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
                            +e.target.value <
                            parseFloat(values.linkThreshold.toString())
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
                            +e.target.value >
                            parseFloat(values.linkThreshold.toString())
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

export default Probabilistic
