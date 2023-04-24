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
      <Typography
        variant="h6"
        sx={{ color: theme => theme.palette.text.primary }}
      >
        MPI
      </Typography>
    </Box>
  )
}

export default JembiLogo
