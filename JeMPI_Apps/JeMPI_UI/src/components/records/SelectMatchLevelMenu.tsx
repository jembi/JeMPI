import {
  FormControl,
  MenuItem,
  OutlinedInput,
  Select,
  SelectChangeEvent
} from '@mui/material'
import { FC } from 'react'

// interface SelectMatchLevelMenuProps {
//   onChange: (() => void) | undefined
// }

const SelectMatchLevelMenu: FC<{
  value: number
  onChange: (value: string) => void
}> = ({ value, onChange }) => {
  const onSelectChange = (event: SelectChangeEvent<string>) => {
    onChange(event.target.value)
  }
  return (
    <FormControl sx={{ '.MuiSelect-select': { p: 0, height: 'auto' } }}>
      <Select
        value={value?.toString()}
        style={{ width: '150px' }}
        input={<OutlinedInput />}
        inputProps={{ 'aria-label': 'Without label' }}
        onChange={onSelectChange}
        sx={{
          boxShadow: 'none',
          '.MuiOutlinedInput-notchedOutline': { border: 0 }
        }}
      >
        <MenuItem disabled value={-2}>
          <em>Select Match Level</em>
        </MenuItem>
        <MenuItem selected value={-1}>
          None
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
