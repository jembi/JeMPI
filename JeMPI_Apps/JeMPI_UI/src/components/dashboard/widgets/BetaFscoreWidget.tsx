import { Box, Grid } from '@mui/material'
import CountWidget from './CountWidgetWidget'

function BetaFscoreWidget() {
  return (
    <Box component={'fieldset'}>
      <legend>Beta F-scores</legend>
      <Grid container spacing={2} columns={16}>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weighs precision higher than recall"
            value={75}
            secondValue={75}
          />
        </Grid>
        <Grid item lg={4} xs={16}>
          <CountWidget label="Neutral" value={75} secondValue={75} />
        </Grid>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weighs recall higher than precision"
            value={75}
            secondValue={75}
          />
        </Grid>
      </Grid>
    </Box>
  )
}

export default BetaFscoreWidget
