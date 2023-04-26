import { Grid } from '@mui/material'
import ToggleButtons from 'components/search/ToggleButtons'
import { useState } from 'react'
import { SearchType } from 'types/ReviewLink'
import { ToggleButtonOptions } from 'types/SimpleSearch'

interface SearchTypeToggleProps {
  options: ToggleButtonOptions[]
  onChange: (
    searchType: SearchType.CUSTOM_SEARCH | SearchType.SIMPLE_SEARCH
  ) => void
}

const SearchTypeToggle: React.FC<SearchTypeToggleProps> = ({
  options,
  onChange
}) => {
  const [selectedButton, setSelectedButton] = useState<string>('0')

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedValue = event?.target.value
    const selectedLabel = options.find(
      ({ value }) => value === Number(selectedValue)
    )
    setSelectedButton(selectedValue)
    onChange(selectedLabel?.label as SearchType)
  }

  return (
    <Grid item container direction={'row'} justifyContent={'center'} mt={3}>
      <ToggleButtons
        selectedButton={selectedButton}
        onChange={handleChange}
        options={options}
      />
    </Grid>
  )
}

export default SearchTypeToggle
