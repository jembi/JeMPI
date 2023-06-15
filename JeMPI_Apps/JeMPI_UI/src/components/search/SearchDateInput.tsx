import { SxProps, TextField, Theme } from '@mui/material'
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import moment, { Moment } from 'moment'
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
  const [dateValue, setDateValue] = useState<Moment | null>(
    value ? moment(value, 'DD/MM/YYYY') : moment()
  )
  const handleChange = (value: Moment | null) => {
    const fieldIdentifier = isCustomRow
      ? `$or[${fieldGroupIndex}].parameters[${index}].${name}`
      : `parameters[${index}].${name}`

    setDateValue(moment(value, 'DD/MM/YYYY'))
    const newDate = moment(value).format('DD/MM/YYYY')
    setFieldValue && setFieldValue(fieldIdentifier, newDate)
  }

  return (
    <LocalizationProvider dateAdapter={AdapterMoment}>
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
