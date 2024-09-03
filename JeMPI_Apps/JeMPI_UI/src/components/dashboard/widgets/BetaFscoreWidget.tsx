import { Box, Grid } from '@mui/material'
import CountWidget from './CountWidgetWidget'

function BetaFscoreWidget({ data, ...rest }: any) {
  return (
    <Box component={'fieldset'}>
      <legend>Beta F-scores</legend>
      <Grid container spacing={2} columns={16}>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weights precision higher than recall"
            value={
              data && typeof data.precision === 'number'
                ? data.precision.toFixed(5)
                : ''
            }
          />
        </Grid>
        <Grid item lg={4} xs={16}>
          <CountWidget
            label="Neutral"
            value={
              data && typeof data.recall_precision === 'number'
                ? data.recall_precision.toFixed(5)
                : ''
            }
          />
        </Grid>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weights recall higher than precision"
            value={
              data && typeof data.recall === 'number'
                ? data.recall.toFixed(5)
                : ''
            }
          />
        </Grid>
      </Grid>
    </Box>
  )
}

export default BetaFscoreWidget
