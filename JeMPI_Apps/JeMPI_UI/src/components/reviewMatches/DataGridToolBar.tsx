import { GridToolbarContainer, GridToolbarQuickFilter } from '@mui/x-data-grid'
import { FC } from 'react'
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { SearchDateInputProps } from 'components/search/SearchDateInput'
import moment from 'moment'
import ApiClient from 'services/ApiClient'

const DataGridToolbar: FC<SearchDateInputProps> = () => {

    // const onClick = () => {
    //     props.sendData(user)
    // }

  const handleChange = () => {
    console.log('Call getMatches from ApiClients ****************************************************')
  }
  return (
    <GridToolbarContainer
      sx={{ p: 2, display: 'flex', gap: 2, alignItems: 'baseLine' }}
    >
      <LocalizationProvider dateAdapter={AdapterMoment}>
        <DesktopDatePicker
            value={moment(moment(), 'YYYY-MM-DD')}
            format="YYYY-MM-DD"
            onChange={handleChange}
            slotProps={{
              textField: {
                variant: 'outlined'
              }
            }}
        />
      </LocalizationProvider>
      <GridToolbarQuickFilter
          placeholder="Search for Notification Type"
          quickFilterParser={searchInput =>
              searchInput.split(',').map(value => value.trim())
          }
          quickFilterFormatter={quickFilterValues =>
              quickFilterValues ? quickFilterValues.join(', ') : ''
          }
          debounceMs={200}
      />
      <GridToolbarQuickFilter
        placeholder="Type to filter results"
        quickFilterParser={searchInput =>
          searchInput.split(',').map(value => value.trim())
        }
        quickFilterFormatter={quickFilterValues =>
          quickFilterValues ? quickFilterValues.join(', ') : ''
        }
        debounceMs={200}
      />
    </GridToolbarContainer>
  )
}

export default DataGridToolbar
