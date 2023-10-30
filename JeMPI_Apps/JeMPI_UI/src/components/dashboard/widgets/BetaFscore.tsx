import { NotificationsOff } from '@mui/icons-material'
import { Box, Grid } from '@mui/material'
import CountWidget from './CountWidget'
import { red } from '@mui/material/colors'

function BetaFscore() {
  return (
    <Box component={'fieldset'}>
      <legend>Beta F-scores</legend>
      <Grid container spacing={2}>
        <Grid item lg={4} xs={12}>
          <CountWidget
            label="Weighs precision higher than recall"
            value={75}
            icon={<NotificationsOff />}
            // green color
            iconBackgroundColor="#76ff03"
          />
        </Grid>
        <Grid item lg={4} xs={12}>
          <CountWidget label="Neutral" value={75} icon={<NotificationsOff />} />
        </Grid>
        <Grid item lg={4} xs={12}>
          <CountWidget
            label="Weighs recall higher than precision"
            value={75}
            icon={<NotificationsOff />}
            iconBackgroundColor={red[500]}
          />
        </Grid>
      </Grid>
    </Box>
  )
}

export default BetaFscore
