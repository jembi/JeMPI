import { Box, Link, Stack, Typography } from '@mui/material'

const CustomSearchHeader = () => {
  return (
    <Box sx={{ mb: 3 }}>
      <Stack direction={'column'}>
        <Typography variant="h5">Customize Your Search Rules</Typography>
        <Typography variant="body2">
          <Link href={'/search/simple'}>Use simple search</Link>
        </Typography>
      </Stack>
    </Box>
  )
}
export default CustomSearchHeader
