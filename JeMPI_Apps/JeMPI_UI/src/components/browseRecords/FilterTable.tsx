import { Box, Button, Paper, TableContainer } from '@mui/material'
import { FC, useState } from 'react'
import { SearchParameter } from 'types/SimpleSearch'
import SearchFormTable from './SearchFormTable'

export const FilterTable: FC<{
  searchButtonLabel?: string
  onSubmit: (query: SearchParameter[]) => void
  onCancel: () => void
}> = ({ searchButtonLabel, onSubmit, onCancel }) => {
  const [query, setQuery] = useState<SearchParameter[]>([])
  const handleCancel = () => {
    setQuery([])
    onCancel()
  }

  return (
    <TableContainer component={Paper}>
      <SearchFormTable onChange={setQuery} />
      <Box p={3} display={'flex'} justifyContent={'flex-end'} gap={'10px'}>
        <Button onClick={() => handleCancel()}>Cancel</Button>
        <Button onClick={() => onSubmit(query)}>
          {searchButtonLabel || 'Search'}
        </Button>
      </Box>
    </TableContainer>
  )
}
