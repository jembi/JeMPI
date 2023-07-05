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
        <MenuItem value={0}>Exact</MenuItem>
        <MenuItem value={1}>Levenshtein 1</MenuItem>
        <MenuItem value={2}>Levenshtein 2</MenuItem>
        <MenuItem value={3}>Levenshtein 3</MenuItem>
      </Select>
    </FormControl>
  )
}

export default SelectMatchLevelMenu
