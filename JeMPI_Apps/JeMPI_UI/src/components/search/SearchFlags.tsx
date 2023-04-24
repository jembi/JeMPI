import { Grid } from '@mui/material'
import { useState } from 'react'
import { ToggleButtonOptions } from '../../types/SimpleSearch'
import ToggleButtons from './ToggleButtons'

interface SearchFlagsProps {
  options: ToggleButtonOptions[]
  onChange: (isGoldenOnly: boolean) => void
}

const SearchFlags: React.FC<SearchFlagsProps> = ({ options, onChange }) => {
  const [selectedButton, setSelectedButton] = useState<string>('0')

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedButton(event?.target.value)
    onChange(event?.target.value === '0')
  }

  return (
    <Grid container direction={'row'} justifyContent={'flex-end'}>
      <Grid item>
        <ToggleButtons
          selectedButton={selectedButton}
          onChange={handleChange}
          options={options}
        />
      </Grid>
    </Grid>
  )
}

export default SearchFlags
