import { Box, Typography } from '@mui/material'
import { LocalizationProvider } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateField } from '@mui/x-date-pickers/DateField'

const DateRangeField: React.FC<{
  onChange: (key: string, value: string | null) => void
}> = ({ onChange }) => {
  const onDateChange = (key: string) => {
    return (value: string | null) => {
      onChange(key, value)
    }
  }
  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          gap: '10px'
        }}
      >
        <Typography>From</Typography>
        <DateField
          variant="standard"
          size="small"
          sx={{ width: 120 }}
          onChange={onDateChange('start')}
        />
        <Typography>to</Typography>
        <DateField
          variant="standard"
          size="small"
          sx={{ width: 120 }}
          onChange={onDateChange('end')}
        />
      </Box>
    </LocalizationProvider>
  )
}

export default DateRangeField
