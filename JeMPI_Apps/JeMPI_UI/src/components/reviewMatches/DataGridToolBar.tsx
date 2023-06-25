import { GridToolbarContainer, GridToolbarQuickFilter } from '@mui/x-data-grid'
import { FC } from 'react'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { Select, MenuItem, InputLabel, FormControl } from '@mui/material'
import { Dayjs } from 'dayjs'

const DataGridToolbar: FC<{
  onChange: (value: Dayjs | null) => void
  value: Dayjs
}> = ({ onChange, value }) => {
  // const onClick = () => {
  //     props.sendData(user)
  // }

  const handleChange = () => {
    console.log(
      'Call getMatches from ApiClients ****************************************************'
    )
  }
  return (
    <GridToolbarContainer
      sx={{ p: 2, display: 'flex', gap: 5, alignItems: 'baseLine' }}
    >
      {/* <FormControl sx={{ minWidth: 200 }}>
        <InputLabel>Notification Type</InputLabel>
        <Select
          renderValue={selected => selected as string}
          placeholder="Search for Notification Type"
          onChange={handleChange}
          sx={{ minWidth: 200 }} // Add this CSS rule to control the width of the select component
        >
          <MenuItem value="type1">Type 1</MenuItem>
          <MenuItem value="type2">Type 2</MenuItem>
          <MenuItem value="type3">Type 3</MenuItem>
        </Select>
      </FormControl> */}
    </GridToolbarContainer>
  )
}

export default DataGridToolbar
