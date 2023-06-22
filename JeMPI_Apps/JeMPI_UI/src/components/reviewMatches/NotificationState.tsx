import { Typography } from '@mui/material'
import { FC, useMemo } from 'react'

const NotificationState: FC<{ value: string }> = ({ value }) => {
  const statusColor = useMemo(() => {
    switch (value) {
      case 'New':
        return 'primary'
      case 'Closed':
        return 'success'
      case 'Accepted':
        return 'success'
      case 'Pending':
        return 'warning'
      default:
        return 'default'
    }
  }, [value])
  return (
    <Typography
      lineHeight={'1.43'}
      fontWeight={400}
      fontSize={'0.875rem'}
      color={statusColor}
    >
      {value}
    </Typography>
  )
}

export default NotificationState
