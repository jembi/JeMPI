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
  onChange: (param: string) => (value: string) => void
}> = ({ onChange }) => {
  const onSelectChange = (event: SelectChangeEvent<string>) => {
    const onValueChange = onChange('distance')
    onValueChange(event.target.value)
  }
  return (
    <FormControl sx={{ '.MuiSelect-select': { p: 0, height: 'auto' } }}>
      <Select
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
        <MenuItem selected value="none">
          None
        </MenuItem>
        <MenuItem value="exact">Exact</MenuItem>
        <MenuItem value="L1">Levenshtein 1</MenuItem>
        <MenuItem value="L2">Levenshtein 2</MenuItem>
        <MenuItem value="L3">Levenshtein 3</MenuItem>
      </Select>
    </FormControl>
  )
}

export default SelectMatchLevelMenu
