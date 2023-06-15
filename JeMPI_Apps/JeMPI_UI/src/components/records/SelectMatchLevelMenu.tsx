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
  onChange: (value: number) => void
}> = ({ value, onChange }) => {
  const onSelectChange = (event: SelectChangeEvent<string>) => {
    onChange(Number(event.target.value))
  }
  return (
    <FormControl sx={{ '.MuiSelect-select': { p: 0, height: 'auto' } }}>
      <Select
        value={value?.toString()}
        defaultValue={'none'}
        style={{ width: '150px' }}
        displayEmpty
        input={<OutlinedInput />}
        inputProps={{ 'aria-label': 'Without label' }}
        onChange={onSelectChange}
        sx={{
          boxShadow: 'none',
          '.MuiOutlinedInput-notchedOutline': { border: 0 }
        }}
      >
        <MenuItem disabled value="">
          <em>Select Match Level</em>
        </MenuItem>
        <MenuItem selected value={NaN}>
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
