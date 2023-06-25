import { Box, Typography } from '@mui/material'

const JembiLogo = () => {
  return (
    <Box
      sx={{
        display: 'flex',
        mr: 4
      }}
    >
      <Typography variant="h6" sx={{ color: '#FFC400' }}>
        Je
      </Typography>
      <Typography variant="h6" sx={{ color: '#274263' }}>
        MPI
      </Typography>
    </Box>
  )
}

export default JembiLogo
