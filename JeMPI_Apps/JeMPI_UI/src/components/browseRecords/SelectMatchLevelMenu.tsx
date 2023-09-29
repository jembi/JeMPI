import {
  FormControl,
  MenuItem,
  OutlinedInput,
  Select,
  SelectChangeEvent
} from '@mui/material'
import { FC, useState } from 'react'

interface SelectMatchLevelMenuProps {
  onChange: (value: string) => void
}

const levenshteinValues: Array<{ key: string; value: number }> = [
  {
    value: 0,
    key: 'Exact'
  },
  {
    value: 1,
    key: 'Levenshtein 1'
  },
  { value: 2, key: 'Levenshtein 2' },
  { value: 3, key: 'Levenshtein 3' }
]

const SelectMatchLevelMenu: FC<SelectMatchLevelMenuProps> = ({ onChange }) => {
  const [selectedValue, setSelectedValue] = useState('0')
  const onSelectChange = (event: SelectChangeEvent<string>) => {
    setSelectedValue(event.target.value)
    onChange(event.target.value)
  }
  return (
    <FormControl sx={{ '.MuiSelect-select': { p: 0, height: 'auto' } }}>
      <Select
        value={selectedValue}
        style={{ width: '150px' }}
        input={<OutlinedInput />}
        inputProps={{ 'aria-label': 'Without label' }}
        onChange={onSelectChange}
        sx={{
          boxShadow: 'none',
          '.MuiOutlinedInput-notchedOutline': { border: 0 }
        }}
      >
        <MenuItem disabled value={-1}>
          <em>Select Match Level</em>
        </MenuItem>
        {levenshteinValues.map(({ key, value }) => (
          <MenuItem key={key} value={value}>
            {key}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  )
}

export default SelectMatchLevelMenu
