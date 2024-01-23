import { Box, Grid } from '@mui/material'
import CountWidget from './CountWidgetWidget'

function BetaFscoreWidget({data, ...rest}: any) {
  return (
    <Box component={'fieldset'}>
      <legend>Beta F-scores</legend>
      <Grid container spacing={2} columns={16}>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weighs precision higher than recall"
            value={data && data.precision.toFixed(2)}
          />
        </Grid>
        <Grid item lg={4} xs={16}>
          <CountWidget label="Neutral" value={data && data.recall_precision.toFixed(2)} />
        </Grid>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weighs recall higher than precision"
            value={data && data.recall.toFixed(2)}
          />
        </Grid>
      </Grid>
    </Box>
  )
}

export default BetaFscoreWidget
