import { Box, Avatar, Typography } from '@mui/material'
import CountUp from 'components/shared/CountUp'
import React from 'react'

type countWidgetType = {
  label: string
  value: number
  icon: React.ReactElement
  iconBackgroundColor?: string
}
function CountWidget({
  label,
  value,
  icon,
  iconBackgroundColor
}: countWidgetType) {
  return (
    <Box
      sx={{
        display: 'flex',
        backgroundColor: 'white',
        alignItems: 'center',
        justifyContent: 'start',
        gap: 2,
        padding: 2,
        borderRadius: 3,
        boxShadow: 3
      }}
    >
      <Avatar
        variant="rounded"
        sx={{
          boxShadow: 3,
          backgroundColor: iconBackgroundColor || 'primary.main',
          width: 75,
          height: 75
        }}
      >
        {icon}
      </Avatar>
      <Box sx={{ display: 'flex', flexDirection: 'column' }}>
        <Typography variant="subtitle2">{label}</Typography>
        <Typography variant="h4">
          <CountUp end={value} duration={1000} />
        </Typography>
      </Box>
    </Box>
  )
}

export default CountWidget
