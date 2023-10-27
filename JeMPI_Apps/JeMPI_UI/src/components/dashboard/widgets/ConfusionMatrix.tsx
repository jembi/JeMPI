import { Box, Divider, Grid, Typography } from '@mui/material'
import React from 'react'

function Item(props: any) {
  const { sx, ...other } = props
  return (
    <Box
      sx={{
        bgcolor: theme => (theme.palette.mode === 'dark' ? '#101010' : '#fff'),
        color: theme =>
          theme.palette.mode === 'dark' ? 'grey.300' : 'grey.800',
        border: '1px solid',
        borderColor: theme =>
          theme.palette.mode === 'dark' ? 'grey.800' : 'grey.300',
        p: 1,
        borderRadius: 2,
        fontSize: '0.875rem',
        fontWeight: '700',
        ...sx
      }}
      {...other}
    />
  )
}
const ConfusionMatrix = () => {
  return (
    <>
      <Grid container padding={1} minHeight={'450px'}>
        <Grid
          item
          lg={6}
          sx={{
            backgroundColor: '#D5E8D4',
            borderBottomLeftRadius: '2rem',
            borderTopLeftRadius: '2rem',
            textAlign: 'center',
            boxShadow: 3
          }}
        >
          <Box sx={{ padding: '1rem' }}>
            <Typography variant="h5">False Negatives</Typography>
            <Typography variant="h3">50</Typography>
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
                {(5000).toLocaleString()}
              </Typography>
            </Box>
          </Box>
        </Grid>
        <Grid
          item
          lg={6}
          sx={{
            backgroundColor: '#F8CECC',
            borderBottomRightRadius: '2rem',
            borderTopRightRadius: '2rem',
            textAlign: 'center',
            boxShadow: 3
          }}
        >
          <Box sx={{ padding: '1rem' }}>
            <Typography variant="h5">True Negatives</Typography>
            <Typography variant="h3">100</Typography>
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
                {(100).toLocaleString()}
              </Typography>
            </Box>
          </Box>
        </Grid>
        <Grid item lg={12}>
          <Grid container padding={2}>
            <Grid item lg={6}>
              <Grid container alignItems={'center'}>
                <Grid item lg={6} textAlign={'center'}>
                  <Typography variant="button">Precesion</Typography>
                </Grid>
                <Grid item lg={6}>
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
                <Grid item lg={6} textAlign={'center'}>
                  (5000 / 5100) =
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
                <Grid item lg={6} textAlign={'center'}>
                  0,98
                </Grid>
                <Grid item lg={6} textAlign={'center'}>
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
            <Grid item lg={6}>
              <Grid container alignItems={'center'}>
                <Grid item lg={6} textAlign={'center'}>
                  <Typography variant="button">Recall</Typography>
                </Grid>
                <Grid item lg={6}>
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
                <Grid item lg={6} textAlign={'center'}>
                  (5000 / 5050) =
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
                <Grid item lg={6} textAlign={'center'}>
                  0,99
                </Grid>
                <Grid item lg={6} textAlign={'center'}>
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
