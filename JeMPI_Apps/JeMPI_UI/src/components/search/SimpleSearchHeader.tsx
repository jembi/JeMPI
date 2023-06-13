import { Grid, Link, Stack, Typography } from '@mui/material'
import { FC } from 'react'

const SimpleSearchHeader: FC<{ isGoldenOnly: boolean }> = ({
  isGoldenOnly
}) => {
  return (
    <Grid item container direction="column" width="fit-content">
      <Grid item sx={{ mb: 1 }}>
        <Stack direction={'row'} spacing={0.5}>
          <Typography variant="body2">
            Find info fast with these fixed fields
          </Typography>
        </Stack>
      </Grid>
    </Grid>
  )
}

export default SimpleSearchHeader
