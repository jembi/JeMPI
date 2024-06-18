import { Typography, Grid, TextField, Slider, Button } from "@mui/material";
import { Box } from "@mui/system";
import { useFormik } from "formik";
import * as Yup from 'yup';

const ProbabilisticContent = () => {
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
  });

  const {
    handleChange: handleImportFormChange,
    handleSubmit,
    values: FormValues,
    setFieldValue,
    errors,
    touched
  } = useFormik({
    initialValues: {
      reportingRequired: false,
      uploadWorkflow: 0,
      minThreshold: 0.65,
      linkThreshold: 0.7,
      maxThreshold: 0.75,
      doNotLinkWindowStart: 0.2,
      doNotLinkWindowEnd: 0.8,
      marginWindowSize: 0.1
    },
    validationSchema,
    onSubmit: () => {
      console.log('submitting', FormValues);
    }
  });

  return (
    <Box
      sx={{
        height: '100vh',
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '20px',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          width: '100%',
          padding: '20px',
        }}
      >
        <form onSubmit={handleSubmit}>
          <Grid container spacing={2} sx={{ marginBottom: '10px', alignItems: 'center' }}>
            <Grid item xs={8}>
              <Slider
                onChange={(_, value: number | number[]) => {
                  if (!Array.isArray(value)) return;
                  const [minThreshold, linkThreshold, maxThreshold] = value;
                  if (
                    0 < linkThreshold &&
                    linkThreshold < 1 &&
                    linkThreshold > minThreshold &&
                    linkThreshold < maxThreshold
                  ) {
                    setFieldValue('linkThreshold', linkThreshold);
                  }
                  if (linkThreshold > minThreshold) setFieldValue('minThreshold', minThreshold);
                  if (linkThreshold < maxThreshold) setFieldValue('maxThreshold', maxThreshold);
                }}
                getAriaValueText={(e: number) => e.toString()}
                valueLabelDisplay="auto"
                step={0.05}
                marks={[
                  { value: 0, label: '0' },
                  { value: 0.5, label: '0.5' },
                  { value: 1, label: '1' },
                ]}
                min={0}
                max={1}
                value={[FormValues.minThreshold, FormValues.linkThreshold, FormValues.maxThreshold]}
                sx={{
                  '& .MuiSlider-thumb': {
                    "&[data-index='0']": { backgroundColor: 'red' },
                    "&[data-index='1']": { backgroundColor: 'green' },
                    "&[data-index='2']": { backgroundColor: 'blue' },
                    "&[data-index='3']": { backgroundColor: 'blue' }
                  }
                }}
                track={false}
              />
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
                    value={FormValues.linkThreshold}
                    onChange={handleImportFormChange}
                    error={touched.linkThreshold && Boolean(errors.linkThreshold)}
                    helperText={touched.linkThreshold && errors.linkThreshold}
                  />
                </Grid>
              </Grid>
              <Grid container spacing={2} sx={{ alignItems: 'center', marginTop: '10px' }}>
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
                    value={FormValues.minThreshold}
                    onChange={handleImportFormChange}
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
                    value={FormValues.maxThreshold}
                    onChange={handleImportFormChange}
                    error={touched.maxThreshold && Boolean(errors.maxThreshold)}
                    helperText={touched.maxThreshold && errors.maxThreshold}
                  />
                </Grid>
              </Grid>
              <Grid container spacing={2} sx={{ alignItems: 'center', marginTop: '10px' }}>
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
                    value={FormValues.doNotLinkWindowStart}
                    onChange={handleImportFormChange}
                    error={touched.doNotLinkWindowStart && Boolean(errors.doNotLinkWindowStart)}
                    helperText={touched.doNotLinkWindowStart && errors.doNotLinkWindowStart}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="do-not-link-window-end"
                    name="doNotLinkWindowEnd"
                    label="End"
                    variant="outlined"
                    size="small"
                    value={FormValues.doNotLinkWindowEnd}
                    onChange={handleImportFormChange}
                    error={touched.doNotLinkWindowEnd && Boolean(errors.doNotLinkWindowEnd)}
                    helperText={touched.doNotLinkWindowEnd && errors.doNotLinkWindowEnd}
                  />
                </Grid>
              </Grid>
              <Grid container spacing={2} sx={{ alignItems: 'center', marginTop: '20px' }}>
                <Grid item xs={3}>
                  <Typography>
                    Margin window size
                  </Typography>
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    id="margin-window-size"
                    name="marginWindowSize"
                    label="Enter floating point number"
                    variant="outlined"
                    size="small"
                    value={FormValues.marginWindowSize}
                    onChange={handleImportFormChange}
                    error={touched.marginWindowSize && Boolean(errors.marginWindowSize)}
                    helperText={touched.marginWindowSize && errors.marginWindowSize}
                  />
                </Grid>
              </Grid>
            </Grid>
            <Grid item xs={4}>
              <Grid container spacing={0} sx={{ alignItems: 'center', height: '100%' }}>
                <Grid item xs={12}>
                <Slider
                min={0}
                max={1}
                value={FormValues.marginWindowSize}
                onChange={(_, value) => {
                  setFieldValue('linkThresholdSlider', value);
                }}
                marks={[                  { value: 0, label: '0' },                  { value: 0.5, label: '0.5' },                  { value: 1, label: '1' },                ]}
                aria-labelledby="margin-window-size-slider"
                sx={{
                  width: '100%',
                  '& .MuiSlider-thumb': { color: "green" },
                  '& .MuiSlider-track': { color: "green" },
                  '& .MuiSlider-rail': { color: "#acc4e4" }
                }}/>

                </Grid>
                <Grid item xs={12}>
                  <Slider
                    min={0}
                    max={1}
                    value={[FormValues.minThreshold, FormValues.maxThreshold]}
                    onChange={(_, value) => {
                      if (!Array.isArray(value)) return;
                      const [minThreshold, maxThreshold] = value;
                      setFieldValue('minThreshold', minThreshold);
                      setFieldValue('maxThreshold', maxThreshold);
                    }}
                    marks={[
                      { value: 0, label: '0' },
                      { value: 0.5, label: '0.5' },
                      { value: 1, label: '1' },
                    ]}
                    aria-labelledby="review-threshold-range-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: "red" },
                      '& .MuiSlider-track': { color: "red" },
                      '& .MuiSlider-rail': { color: "#acc4e4" }
                    }}
                  />
                </Grid>
                <Grid item xs={12} sx={{ marginTop: '20px' }}>
                  <Slider
                    min={0}
                    max={1}
                    value={[FormValues.doNotLinkWindowStart, FormValues.doNotLinkWindowEnd]}
                    onChange={(_, value) => {
                      if (!Array.isArray(value)) return;
                      const [doNotLinkWindowStart, doNotLinkWindowEnd] = value;
                      setFieldValue('doNotLinkWindowStart', doNotLinkWindowStart);
                      setFieldValue('doNotLinkWindowEnd', doNotLinkWindowEnd);
                    }}
                    marks={[
                      { value: 0, label: '0' },
                      { value: 0.5, label: '0.5' },
                      { value: 1, label: '1' },
                    ]}
                    aria-labelledby="do-not-link-window-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: "blue" },
                      '& .MuiSlider-track': { color: "blue" },
                      '& .MuiSlider-rail': { color: "#acc4e4" }
                    }}
                  />
                </Grid>
                <Grid item xs={12} sx={{ marginTop: '20px' }}>
                  <Slider
                    min={0}
                    max={1}
                    value={FormValues.marginWindowSize}
                    onChange={(_, value) => {
                      setFieldValue('marginWindowSize', value);
                    }}
                    marks={[
                      { value: 0, label: '0' },
                      { value: 0.5, label: '0.5' },
                      { value: 1, label: '1' },
                    ]}
                    aria-labelledby="margin-window-size-slider"
                    sx={{
                      width: '100%',
                      '& .MuiSlider-thumb': { color: "gray" },
                      '& .MuiSlider-track': { color: "gray" },
                      '& .MuiSlider-rail': { color: "#acc4e4" }
                    }}
                  />
                </Grid>
              </Grid>
            </Grid>
          </Grid>

          <Box sx={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
            <Button variant="outlined">Edit</Button>
            <Box sx={{ justifyContent: 'space-evenly' }}>
              <Button variant="outlined" sx={{ marginRight: '10px' }} type="submit">Save</Button>
              <Button variant="outlined">Generate</Button>
            </Box>
          </Box>
        </form>
      </Box>
    </Box>
  );
};

export default ProbabilisticContent;
