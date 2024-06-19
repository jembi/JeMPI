import { useEffect } from 'react'
import { Typography, Grid, TextField, Slider, Button } from '@mui/material'
import { Box } from '@mui/system'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import { defaultValues, marks } from './ProbabilisticConstants'
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
}

const ProbabilisticContent = ({
  linkingRules = {}
}: ProbabilisticContentProps) => {
  const { configuration, setConfiguration } = useConfiguration()

  const probabilisticRules = [
    ...(linkingRules.link?.probabilistic || []),
    ...(linkingRules.validate?.probabilistic || []),
    ...(linkingRules.matchNotification?.probabilistic || [])
  ]

  const rule = probabilisticRules[0] || {}

  const initialValues = {
    minThreshold:
      rule.reviewThresholdRange?.low.toString() || defaultValues.minThreshold,
    linkThreshold:
      rule.linkThreshold?.toString() || defaultValues.linkThreshold,
    maxThreshold:
      rule.reviewThresholdRange?.high.toString() || defaultValues.maxThreshold,
    doNotLinkWindowStart:
      rule.doNotLinkWindow?.low.toString() ||
      defaultValues.doNotLinkWindowStart,
    doNotLinkWindowEnd:
      rule.doNotLinkWindow?.high.toString() || defaultValues.doNotLinkWindowEnd,
    marginWindowSize:
      rule.marginWindowSize?.toString() || defaultValues.marginWindowSize
  }

  const validationSchema = Yup.object({
    linkThreshold: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    minThreshold: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    maxThreshold: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    doNotLinkWindowStart: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    doNotLinkWindowEnd: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    marginWindowSize: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required')
  })

  const { handleChange, handleSubmit, values, setFieldValue, errors, touched } =
    useFormik({
      initialValues,
      validationSchema,
      onSubmit: values => {
        const finalValues = {
          minThreshold:
            parseFloat(values.minThreshold) ||
            parseFloat(defaultValues.minThreshold),
          linkThreshold:
            parseFloat(values.linkThreshold) ||
            parseFloat(defaultValues.linkThreshold),
          maxThreshold:
            parseFloat(values.maxThreshold) ||
            parseFloat(defaultValues.maxThreshold),
          doNotLinkWindowStart:
            parseFloat(values.doNotLinkWindowStart) ||
            parseFloat(defaultValues.doNotLinkWindowStart),
          doNotLinkWindowEnd:
            parseFloat(values.doNotLinkWindowEnd) ||
            parseFloat(defaultValues.doNotLinkWindowEnd),
          marginWindowSize:
            parseFloat(values.marginWindowSize) ||
            parseFloat(defaultValues.marginWindowSize)
        }

        const handleUpdateConfiguration = () => {
          setConfiguration(prevConfig => {
            if (!prevConfig) return prevConfig

            const updatedConfiguration: Configuration = {
              ...prevConfig,
              rules: {
                ...prevConfig.rules,
                matchNotification: {
                  ...prevConfig.rules.matchNotification,
                  probabilistic: [
                    {
                      ...rule,
                      linkThreshold: finalValues.linkThreshold,
                      reviewThresholdRange: {
                        low: finalValues.minThreshold,
                        high: finalValues.maxThreshold
                      },
                      doNotLinkWindow: {
                        low: finalValues.doNotLinkWindowStart,
                        high: finalValues.doNotLinkWindowEnd
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

        handleUpdateConfiguration()
        localStorage.setItem('configuration', JSON.stringify(configuration))
      }
    })

  const handleValidatedChange = (field: string, value: string) => {
    const parsedValue = parseFloat(value)
    switch (field) {
      case 'linkThreshold':
        if (
          parsedValue > parseFloat(values.minThreshold) &&
          parsedValue < parseFloat(values.maxThreshold)
        ) {
          setFieldValue(field, value)
        }
        break
      case 'minThreshold':
        if (parsedValue < parseFloat(values.linkThreshold)) {
          setFieldValue(field, value)
        }
        break
      case 'maxThreshold':
        if (parsedValue > parseFloat(values.linkThreshold)) {
          setFieldValue(field, value)
        }
        break
      case 'doNotLinkWindowStart':
        if (parsedValue < parseFloat(values.minThreshold)) {
          setFieldValue(field, value)
        }
        break
      case 'doNotLinkWindowEnd':
        if (parsedValue > parseFloat(values.maxThreshold)) {
          setFieldValue(field, value)
        }
        break
      default:
        setFieldValue(field, value)
        break
    }
  }

  return (
    <Box
      sx={{
        height: '70vh',
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
            sx={{ marginBottom: '10px', alignItems: 'center' }}
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
                parseFloat(values.minThreshold),
                parseFloat(values.linkThreshold),
                parseFloat(values.maxThreshold),
                parseFloat(values.doNotLinkWindowEnd),
                parseFloat(values.doNotLinkWindowStart)
              ]}
              sx={{
                width: '50%',
                '& .MuiSlider-thumb': {
                  "&[data-index='0']": { backgroundColor: 'red' },
                  "&[data-index='1']": { backgroundColor: 'green' },
                  "&[data-index='2']": { backgroundColor: 'red' },
                  "&[data-index='3']": { backgroundColor: 'blue' },
                  "&[data-index='4']": { backgroundColor: 'blue' }
                }
              }}
              track={false}
            />

            <Grid item xs={8}>
              <Grid container spacing={2} sx={{ alignItems: 'center' }}>
                <Grid item xs={3}>
                  <Typography sx={{ color: 'green' }}>
                    Link Threshold
                  </Typography>
                </Grid>
                <Grid item xs={3}>
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
                  />
                </Grid>
              </Grid>
              <Grid
                container
                spacing={2}
                sx={{ alignItems: 'center', marginTop: '10px' }}
              >
                <Grid item xs={3}>
                  <Typography sx={{ color: 'red' }}>
                    Review Threshold range
                  </Typography>
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="min-threshold"
                    name="minThreshold"
                    label="Min"
                    variant="outlined"
                    size="small"
                    value={values.minThreshold}
                    onChange={e =>
                      handleValidatedChange('minThreshold', e.target.value)
                    }
                    error={touched.minThreshold && Boolean(errors.minThreshold)}
                    helperText={touched.minThreshold && errors.minThreshold}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="max-threshold"
                    name="maxThreshold"
                    label="Max"
                    variant="outlined"
                    size="small"
                    value={values.maxThreshold}
                    onChange={e =>
                      handleValidatedChange('maxThreshold', e.target.value)
                    }
                    error={touched.maxThreshold && Boolean(errors.maxThreshold)}
                    helperText={touched.maxThreshold && errors.maxThreshold}
                  />
                </Grid>
              </Grid>
              <Grid
                container
                spacing={2}
                sx={{ alignItems: 'center', marginTop: '10px' }}
              >
                <Grid item xs={3}>
                  <Typography sx={{ color: 'blue' }}>
                    Do not link window
                  </Typography>
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="do-not-link-window-start"
                    name="doNotLinkWindowStart"
                    label="Start"
                    variant="outlined"
                    size="small"
                    value={values.doNotLinkWindowStart}
                    onChange={e =>
                      handleValidatedChange(
                        'doNotLinkWindowStart',
                        e.target.value
                      )
                    }
                    error={
                      touched.doNotLinkWindowStart &&
                      Boolean(errors.doNotLinkWindowStart)
                    }
                    helperText={
                      touched.doNotLinkWindowStart &&
                      errors.doNotLinkWindowStart
                    }
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="do-not-link-window-end"
                    name="doNotLinkWindowEnd"
                    label="End"
                    variant="outlined"
                    size="small"
                    value={values.doNotLinkWindowEnd}
                    onChange={e =>
                      handleValidatedChange(
                        'doNotLinkWindowEnd',
                        e.target.value
                      )
                    }
                    error={
                      touched.doNotLinkWindowEnd &&
                      Boolean(errors.doNotLinkWindowEnd)
                    }
                    helperText={
                      touched.doNotLinkWindowEnd && errors.doNotLinkWindowEnd
                    }
                  />
                </Grid>
              </Grid>
              <Grid
                container
                spacing={2}
                sx={{ alignItems: 'center', marginTop: '20px' }}
              >
                <Grid item xs={3}>
                  <Typography>Margin window size</Typography>
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="margin-window-size"
                    name="marginWindowSize"
                    label="Enter floating point number"
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
                  />
                </Grid>
              </Grid>
            </Grid>
            <Grid item xs={4}>
              <Grid container spacing={2} direction="column">
                <Grid item xs={12}>
                  <Slider
                    min={0}
                    max={1}
                    value={parseFloat(values.linkThreshold)}
                    onChange={(_, value) => {
                      const linkThreshold = parseFloat(value.toString())
                      if (
                        linkThreshold > parseFloat(values.minThreshold) &&
                        linkThreshold < parseFloat(values.maxThreshold)
                      ) {
                        setFieldValue('linkThreshold', value.toString())
                      }
                    }}
                    marks={marks}
                    aria-labelledby="link-threshold-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: 'green' }
                    }}
                    track={false}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Slider
                    min={0}
                    max={1}
                    value={[
                      parseFloat(values.minThreshold),
                      parseFloat(values.maxThreshold)
                    ]}
                    onChange={(_, value) => {
                      if (!Array.isArray(value)) return
                      const [minThreshold, maxThreshold] = value
                      if (
                        minThreshold < parseFloat(values.linkThreshold) &&
                        maxThreshold > parseFloat(values.linkThreshold)
                      ) {
                        setFieldValue('minThreshold', minThreshold.toString())
                        setFieldValue('maxThreshold', maxThreshold.toString())
                      }
                    }}
                    marks={marks}
                    aria-labelledby="review-threshold-range-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: 'red' },
                      '& .MuiSlider-track': { color: 'red' },
                      '& .MuiSlider-rail': { color: '#acc4e4' }
                    }}
                    track={false}
                  />
                </Grid>
                <Grid item xs={12} sx={{ marginTop: '20px' }}>
                  <Slider
                    min={0}
                    max={1}
                    value={[
                      parseFloat(values.doNotLinkWindowStart),
                      parseFloat(values.doNotLinkWindowEnd)
                    ]}
                    onChange={(_, value) => {
                      if (!Array.isArray(value)) return
                      const [doNotLinkWindowStart, doNotLinkWindowEnd] = value
                      if (
                        doNotLinkWindowStart <
                          parseFloat(values.minThreshold) &&
                        doNotLinkWindowEnd > parseFloat(values.maxThreshold)
                      ) {
                        setFieldValue(
                          'doNotLinkWindowStart',
                          doNotLinkWindowStart.toString()
                        )
                        setFieldValue(
                          'doNotLinkWindowEnd',
                          doNotLinkWindowEnd.toString()
                        )
                      }
                    }}
                    marks={marks}
                    aria-labelledby="do-not-link-window-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: 'blue' },
                      '& .MuiSlider-track': { color: 'blue' },
                      '& .MuiSlider-rail': { color: '#acc4e4' }
                    }}
                    track={false}
                  />
                </Grid>
                <Grid item xs={12} sx={{ marginTop: '20px' }}>
                  <Slider
                    min={0}
                    max={1}
                    value={parseFloat(values.marginWindowSize)}
                    onChange={(_, value) => {
                      setFieldValue('marginWindowSize', value.toString())
                    }}
                    marks={marks}
                    aria-labelledby="margin-window-size-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: 'gray' },
                      '& .MuiSlider-track': { color: 'gray' },
                      '& .MuiSlider-rail': { color: '#acc4e4' }
                    }}
                    track={false}
                  />
                </Grid>
              </Grid>
            </Grid>
          </Grid>
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              marginTop: '20px'
            }}
          >
            <Button variant="outlined">Edit</Button>
            <Box sx={{ justifyContent: 'space-evenly' }}>
              <Button
                variant="outlined"
                sx={{ marginRight: '10px' }}
                type="submit"
              >
                Save
              </Button>
              <Button variant="outlined">Generate</Button>
            </Box>
          </Box>
        </form>
      </Box>
    </Box>
  )
}

export default ProbabilisticContent
