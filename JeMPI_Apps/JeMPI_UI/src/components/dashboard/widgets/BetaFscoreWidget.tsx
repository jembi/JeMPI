import React from 'react'
import { Box, Grid } from '@mui/material'
import CountWidget from './CountWidgetWidget'

interface DataProps {
  precision?: number
  recall_precision?: number
  recall?: number
}

interface BetaFscoreWidgetProps {
  data: DataProps
}

function BetaFscoreWidget({ data }: BetaFscoreWidgetProps) {
  const formatValue = (value?: number): number =>
    value !== undefined && Number.isFinite(value)
      ? parseFloat(value.toFixed(5))
      : 0

  return (
    <Box component="fieldset">
      <legend>Beta F-scores</legend>
      <Grid container spacing={2} columns={16}>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weights precision higher than recall"
            value={formatValue(data?.precision)}
          />
        </Grid>
        <Grid item lg={4} xs={16}>
          <CountWidget
            label="Neutral"
            value={formatValue(data?.recall_precision)}
          />
        </Grid>
        <Grid item lg={6} xs={16}>
          <CountWidget
            label="Weights recall higher than precision"
            value={formatValue(data?.recall)}
          />
        </Grid>
      </Grid>
    </Box>
  )
}

export default BetaFscoreWidget
