import { CircularProgressProps, CircularProgress, Typography, LinearProgressProps, LinearProgress } from '@mui/material'
import { Box } from '@mui/system'
import React, { useEffect, useState } from 'react'

function CircularProgressWithLabel(
  props: CircularProgressProps & { value: number }
) {
  return (
    <Box sx={{ position: 'relative', display: 'inline-flex' }}>
      <CircularProgress variant="determinate" {...props} />
      <Box
        sx={{
          top: 0,
          left: 0,
          bottom: 0,
          right: 0,
          position: 'absolute',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        <Typography
          variant="caption"
          component="div"
          color="text.secondary"
        >{`${Math.round(props.value)}%`}</Typography>
      </Box>
    </Box>
  )
}

function LinearProgressWithLabel(
  props: LinearProgressProps & { value: number }
) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center' }}>
      <Box sx={{ width: '100%', mr: 1 }}>
        <LinearProgress variant="determinate" {...props} />
      </Box>
      <Box sx={{ minWidth: 35 }}>
        <Typography variant="body2" color="text.secondary">{`${Math.round(
          props.value
        )}%`}</Typography>
      </Box>
    </Box>
  )
}
export const ImportProcessWidget = ({data, ...rest}: any) => {

  let progress = 0

  if (data){
    // TODO: data.toFileSize * 4 is just rough estimate. Need to this properly later
    progress = data.sizeCompleted >= (data.toFileSize * 4) ? 100 : (data.sizeCompleted/(data.toFileSize * 4)) * 100
  }

  return (
    <Box component={'fieldset'}>
      <legend>Import Process</legend>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          padding: 2
        }}
      >
        <CircularProgressWithLabel value={progress} /> {data && data.filename}
        <Box sx={{ width: '360px' }}>
          <LinearProgressWithLabel value={progress} />
        </Box>
        <Typography variant="h6" color="text.secondary">
          {(data ? data.totalCompleted : 0) + ' Processed'}
        </Typography>
      </Box>
    </Box>
  )
}
