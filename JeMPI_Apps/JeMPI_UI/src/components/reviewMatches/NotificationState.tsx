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
  return <Typography color={statusColor}>{value}</Typography>
}

export default NotificationState
