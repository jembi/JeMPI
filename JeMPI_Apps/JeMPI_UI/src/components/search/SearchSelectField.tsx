import {
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  SxProps,
  Theme
} from '@mui/material'

interface optionsProp {
  value: string | number
  label: string
}
interface SearchSelectFieldProps {
  index: number
  onChange: ((event: SelectChangeEvent<string>) => void) | undefined
  fieldName: string
  options: optionsProp[]
  title: string
  description?: string
  helperText?: string
  sx?: SxProps<Theme>
  name: string
  size?: 'small' | 'medium' | undefined
  fieldGroupIndex?: number
  isCustomRow: boolean
}

const SearchSelectField: React.FC<SearchSelectFieldProps> = ({
  onChange,
  fieldName,
  options,
  title,
  description,
  helperText,
  sx = { width: 200 },
  name,
  size = 'medium',
  index,
  fieldGroupIndex,
  isCustomRow
}) => {
  const fieldIdentifier = isCustomRow
    ? `$or[${fieldGroupIndex}].parameters[${index}].${name}`
    : `parameters[${index}].${name}`

  return (
    <FormControl fullWidth sx={sx} size={size}>
      <InputLabel>{title}</InputLabel>
      <Select
        value={fieldName}
        label="Field"
        onChange={onChange}
        name={fieldIdentifier}
      >
        <MenuItem value="" disabled>
          <em>{description}</em>
        </MenuItem>
        {options.map((item, index) => {
          return (
            <MenuItem value={item.value} key={index}>
              {item.label}
            </MenuItem>
          )
        })}
      </Select>
      <FormHelperText>{helperText || ''}</FormHelperText>
    </FormControl>
  )
}

export default SearchSelectField
