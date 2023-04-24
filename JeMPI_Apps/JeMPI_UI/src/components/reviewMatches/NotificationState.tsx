import { Chip } from '@mui/material'
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
  return <Chip variant="outlined" label={value} color={statusColor} />
}

export default NotificationState
