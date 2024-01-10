import { Box, Divider, Grid, Typography } from '@mui/material'

const ConfusionMatrix = ({data, ...rest}: any) => {
  return (
    <>
      <Grid container padding={1} minHeight={'450px'}>
        <Grid
          item
          lg={6}
          xs={12}
          sx={{
            backgroundColor: '#D5E8D4',
            borderBottomLeftRadius: { lg: '2rem' },
            borderTopLeftRadius: { lg: '2rem' },
            textAlign: 'center',
            boxShadow: 3
          }}
        >
          <Box sx={{ padding: '1rem' }}>
            <Typography variant="h5">False Negatives</Typography>
            <Typography variant="h3">{data && data.tptnMatrix.falseNegative}</Typography>
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'end' }}>
            <Box
              sx={{
                width: '50%',
                padding: '1rem',
                border: '1px solid #9FD383',
                backgroundColor: '#9FD383',
                borderRadius: '1rem 0rem 0rem 1rem',
                boxShadow: 3
              }}
            >
              <Typography variant="h5" color={'#fff'}>
                True Positives
              </Typography>

              <Typography variant="h3" color={'#fff'}>
                {data && data.tptnMatrix.truePositive.toLocaleString()}
              </Typography>
            </Box>
          </Box>
        </Grid>
        <Grid
          item
          lg={6}
          xs={12}
          sx={{
            backgroundColor: '#F8CECC',
            borderBottomRightRadius: { lg: '2rem' },
            borderTopRightRadius: { lg: '2rem' },
            textAlign: 'center',
            boxShadow: 3
          }}
        >
          <Box sx={{ padding: '1rem' }}>
            <Typography variant="h5">True Negatives</Typography>
            <Typography variant="h3">{data && data.tptnMatrix.trueNegative}</Typography>
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'start' }}>
            <Box
              sx={{
                width: '50%',
                padding: '1rem',
                border: '1px solid #EC7974',
                backgroundColor: '#EC7974',
                borderRadius: '0rem 1rem 1rem 0rem',

                boxShadow: 3
              }}
            >
              <Typography variant="h5" color={'#fff'}>
                {'False Positives'}
              </Typography>
              <Typography variant="h3" color={'#fff'}>
                {data && data.tptnMatrix.falsePositive.toLocaleString()}
              </Typography>
            </Box>
          </Box>
        </Grid>
        <Grid item xs={12}>
          <Grid container padding={{ lg: '2rem 1rem 1rem 1rem', xs: '0.5rem' }}>
            <Grid item xs={6}>
              <Grid container alignItems={'center'}>
                <Grid item xs={6} textAlign={'center'}>
                  <Typography variant="button">Precesion</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Box
                    sx={{
                      width: '30px',
                      height: '40px',
                      marginLeft: '5px',
                      backgroundColor: '#9FD383',
                      boxShadow: 3
                    }}
                  ></Box>
                </Grid>
                <Grid item xs={6} textAlign={'center'}>
                  {data && `${data.tptnMatrix.truePositive} / ${data.tptnMatrix.truePositive + data.tptnMatrix.falsePositive} =`}
                </Grid>
                <Grid
                  item
                  lg={6}
                  minHeight={'50px'}
                  textAlign={'start'}
                  justifyContent={'center'}
                  display={'flex'}
                  flexDirection={'column'}
                  alignItems={'start'}
                  justifyItems={'start'}
                >
                  <Divider component={'button'} sx={{ width: '40px' }} />
                </Grid>
                <Grid item xs={6} textAlign={'center'}>
                  {data && (data.tptnMatrix.truePositive / (data.tptnMatrix.truePositive + data.tptnMatrix.falsePositive)).toFixed(2)}
                </Grid>
                <Grid item xs={6} textAlign={'center'}>
                  <Box
                    sx={{
                      width: '40px',
                      height: '40px',
                      background:
                        'linear-gradient(to right, #9FD383 50%, #EC7974 50%)',
                      boxShadow: 3
                    }}
                  ></Box>
                </Grid>
              </Grid>
            </Grid>
            <Grid item xs={6}>
              <Grid container alignItems={'center'}>
                <Grid item xs={6} textAlign={'center'}>
                  <Typography variant="button">Recall</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Box
                    sx={{
                      width: '30px',
                      height: '40px',
                      marginLeft: '5px',
                      backgroundColor: '#9FD383',
                      boxShadow: 3
                    }}
                  ></Box>
                </Grid>
                <Grid item xs={6} textAlign={'center'}>
                {data && `${data.tptnMatrix.truePositive} / ${data.tptnMatrix.truePositive + data.tptnMatrix.falseNegative} =`}
                </Grid>
                <Grid
                  item
                  xs={6}
                  minHeight={'50px'}
                  textAlign={'start'}
                  justifyContent={'center'}
                  display={'flex'}
                  flexDirection={'column'}
                  alignItems={'start'}
                  justifyItems={'start'}
                >
                  <Divider component={'button'} sx={{ width: '40px' }} />
                </Grid>
                <Grid item xs={6} textAlign={'center'}>
                  {data && (data.tptnMatrix.truePositive / (data.tptnMatrix.truePositive + data.tptnMatrix.falseNegative)).toFixed(2) }
                </Grid>
                <Grid item xs={6} textAlign={'center'}>
                  <Box
                    sx={{
                      width: '40px',
                      height: '40px',
                      background: '#D5E8D4',
                      display: 'flex',
                      justifyContent: 'end',
                      alignItems: 'center',
                      paddingY: '3px',
                      paddingRight: '3px'
                    }}
                  >
                    <Box
                      sx={{
                        width: '25px',
                        height: '32px',
                        background: '#9FD383',
                        boxShadow: 3
                      }}
                    ></Box>
                  </Box>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </>
  )
}

export default ConfusionMatrix
