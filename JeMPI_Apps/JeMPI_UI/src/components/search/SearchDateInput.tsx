import { SxProps, Theme } from '@mui/material'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import dayjs, { Dayjs } from 'dayjs'
import { useState } from 'react'

export interface SearchDateInputProps {
  name: string
  value: string | Date
  label: string
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void | null
  sx: SxProps<Theme>
  size: 'small' | 'medium' | undefined
  setFieldValue?: (
    field: string,
    value: Date | string | number,
    shouldValidate?: boolean | undefined
  ) => void
  fieldGroupIndex?: number
  index: number
  isCustomRow?: boolean
}

const SearchDateInput: React.FC<SearchDateInputProps> = ({
  name,
  value,
  label,
  onChange,
  sx = { width: 220 },
  size = 'small',
  setFieldValue,
  index,
  fieldGroupIndex,
  isCustomRow
}) => {
  const [dateValue, setDateValue] = useState<Dayjs | null>(
    value ? dayjs(value, 'DD/MM/YYYY') : dayjs()
  )
  const handleChange = (value: Dayjs | null) => {
    const fieldIdentifier = isCustomRow
      ? `$or[${fieldGroupIndex}].parameters[${index}].${name}`
      : `parameters[${index}].${name}`

    setDateValue(dayjs(value, 'DD/MM/YYYY'))
    const newDate = dayjs(value).format('DD/MM/YYYY')
    setFieldValue && setFieldValue(fieldIdentifier, newDate)
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <DesktopDatePicker
        label={label}
        format="DD/MM/YYYY"
        value={dateValue}
        onChange={handleChange}
        slotProps={{
          textField: {
            name: name,
            variant: 'outlined',
            size: size,
            onChange: onChange,
            sx: sx
          }
        }}
      />
    </LocalizationProvider>
  )
}

export default SearchDateInput
