import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider
} from '@mui/material'
import CustomSearchForm from 'components/customSearch/CustomSearchForm'
import { FC, useState } from 'react'
import { SearchType } from 'types/ReviewLink'
import {
  CustomSearchQuery,
  SearchQuery,
  ToggleButtonOptions
} from 'types/SimpleSearch'
import SearchTypeToggle from './SearchTypeToggle'

import SearchFormTable from 'components/browseRecords/SearchFormTable'
import { PAGINATION_LIMIT } from 'utils/constants'

const options: ToggleButtonOptions[] = [
  { value: 0, label: SearchType.CUSTOM_SEARCH },
  { value: 1, label: SearchType.SIMPLE_SEARCH }
]

const SearchModal: FC<{
  isOpen: boolean
  onClose: () => void
  onChange: (query: SearchQuery | CustomSearchQuery | undefined) => void
}> = ({ isOpen, onClose, onChange }) => {
  const [selectedTab, setSelectedTab] = useState(SearchType.CUSTOM_SEARCH)
  const [refineSearchQuery, setRefineSearchQuery] = useState<
    SearchQuery | CustomSearchQuery | undefined
  >(undefined)

  const handleClose = () => {
    onClose()
  }

  const onConfirmSearch = () => {
    onChange(refineSearchQuery)
    onClose()
  }

  return (
    <Dialog fullWidth maxWidth={'xl'} open={isOpen}>
      <DialogContent sx={{ p: 0 }}>
        <DialogTitle>Refine the current search</DialogTitle>
        <Divider />
        <SearchTypeToggle onChange={setSelectedTab} options={options} />

        {selectedTab === SearchType.SIMPLE_SEARCH && (
          <Box mt={3}>
            <SearchFormTable
              onChange={query =>
                setRefineSearchQuery({
                  parameters: query,
                  sortBy: 'uid',
                  sortAsc: false,
                  offset: 0,
                  limit: PAGINATION_LIMIT
                })
              }
            />
          </Box>
        )}
        {selectedTab === SearchType.CUSTOM_SEARCH && (
          <Box mt={3}>
            <CustomSearchForm onChange={setRefineSearchQuery} />
          </Box>
        )}

        <DialogActions
          sx={{ display: 'flex', justifyContent: 'flex-end', p: '20px' }}
        >
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={onConfirmSearch}>Search</Button>
        </DialogActions>
      </DialogContent>
    </Dialog>
  )
}

export default SearchModal
