import { GridToolbarContainer, GridToolbarQuickFilter } from '@mui/x-data-grid'
import { FC } from 'react'

const DataGridToolbar: FC = () => {
  return (
    <GridToolbarContainer
      sx={{ p: 2, display: 'flex', gap: 2, alignItems: 'baseLine' }}
    >
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
