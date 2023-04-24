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
import SimpleSearchForm from 'components/search/SimpleSearchForm'
import { FC, useState } from 'react'
import { SearchType } from 'types/ReviewLink'
import {
  CustomSearchQuery,
  SearchQuery,
  ToggleButtonOptions
} from 'types/SimpleSearch'
import SearchTypeToggle from './SearchTypeToggle'

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
    <Dialog fullWidth maxWidth={'md'} open={isOpen}>
      <DialogContent sx={{ p: 0 }}>
        <DialogTitle>Refine the current search</DialogTitle>
        <Divider />
        <SearchTypeToggle onChange={setSelectedTab} options={options} />

        {selectedTab === SearchType.SIMPLE_SEARCH && (
          <SimpleSearchForm onChange={setRefineSearchQuery} />
        )}
        {selectedTab === SearchType.CUSTOM_SEARCH && (
          <Box mt={3}>
            <CustomSearchForm onChange={setRefineSearchQuery} />
          </Box>
        )}

        <DialogActions
          sx={{ display: 'flex', justifyContent: 'space-between', p: '20px' }}
        >
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={onConfirmSearch}>Refine Search</Button>
        </DialogActions>
      </DialogContent>
    </Dialog>
  )
}

export default SearchModal
