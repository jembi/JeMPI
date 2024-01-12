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
export const ImportProcessWidget = () => {
  const [progress, setProgress] = useState(10)
  useEffect(() => {
    const timer = setInterval(() => {
      const randomIncrement = Math.floor(Math.random() * 10)

      setProgress(prevProgress =>
        prevProgress >= 100 ? 0 : prevProgress + randomIncrement
      )
    }, 800)
    return () => {
      clearInterval(timer)
    }
  }, [])
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
        <CircularProgressWithLabel value={progress} /> imported_file_name.csv
        <Box sx={{ width: '360px' }}>
          <LinearProgressWithLabel value={progress} />
        </Box>
        <Typography variant="h6" color="text.secondary">
          {((progress / 100) * 1958000).toLocaleString() + '/ 1,958,000'}
        </Typography>
      </Box>
    </Box>
  )
}
