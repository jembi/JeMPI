import { Box, Typography } from '@mui/material'

const JembiLogo = () => {
  return (
    <Box
      sx={{
        display: 'flex',
        fontSize: { xs: '1.5rem', md: '4rem' },
        mr: 4
      }}
    >
      <Typography
        sx={{ color: '#FFC400', fontSize: { xs: '2rem', md: '3rem' } }}
      >
        Je
      </Typography>
      <Typography
        sx={{ color: '#274263', fontSize: { xs: '2rem', md: '3rem' } }}
      >
        MPI
      </Typography>
    </Box>
  )
}

export default JembiLogo
