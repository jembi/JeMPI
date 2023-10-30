import { Box, Grid } from '@mui/material'
import CountWidget from './CountWidget'
import { red } from '@mui/material/colors'

function BetaFscoreWidget() {
  return (
    <Box component={'fieldset'}>
      <legend>Beta F-scores</legend>
      <Grid container spacing={2} columns={16}>
        <Grid item lg={6}>
          <CountWidget
            label="Weighs precision higher than recall"
            value={75}
            // green color
            iconBackgroundColor="#76ff03"
          />
        </Grid>
        <Grid item lg={4}>
          <CountWidget label="Neutral" value={75} />
        </Grid>
        <Grid item lg={6}>
          <CountWidget
            label="Weighs recall higher than precision"
            value={75}
            iconBackgroundColor={red[500]}
          />
        </Grid>
      </Grid>
    </Box>
  )
}

export default BetaFscoreWidget
