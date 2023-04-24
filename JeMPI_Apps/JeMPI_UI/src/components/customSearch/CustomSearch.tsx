import { MoreHorizOutlined } from '@mui/icons-material'
import SearchIcon from '@mui/icons-material/Search'
import { Button, Card, Container, Grid, Stack } from '@mui/material'
import Divider from '@mui/material/Divider'
import { Link as LocationLink } from '@tanstack/react-location'
import { useState } from 'react'
import {
  CustomSearchQuery,
  FlagLabel,
  ToggleButtonOptions
} from '../../types/SimpleSearch'
import SearchFlags from '../search/SearchFlags'
import PageHeader from '../shell/PageHeader'
import CustomSearchForm from './CustomSearchForm'
import CustomSearchHeader from './CustomSearchHeader'

const CustomSearch: React.FC = () => {
  const [isGoldenOnly, setIsGoldenOnly] = useState<boolean>(true)
  const [customSearchQuerry, setCustomSearchQuerry] = useState<
    CustomSearchQuery | undefined
  >(undefined)
  const options: ToggleButtonOptions[] = [
    { value: 0, label: FlagLabel.GOLDEN_ONLY },
    { value: 1, label: FlagLabel.PATIENT_ONLY }
  ]

  return (
    <>
      <Container maxWidth={false}>
        <Grid container direction={'column'}>
          <Grid item lg={6}>
            <PageHeader
              description="Tailor your search to quickly find the information you need."
              title="Custom Search"
              breadcrumbs={[
                {
                  icon: <MoreHorizOutlined />
                },
                {
                  icon: <SearchIcon />,
                  title: 'Search'
                }
              ]}
              buttons={[
                <SearchFlags
                  options={options}
                  onChange={setIsGoldenOnly}
                  key="search-flags"
                />,
                <Button
                  variant="outlined"
                  href={'/search/simple'}
                  key="simple-search"
                  size="large"
                >
                  SIMPLE SEARCH
                </Button>
              ]}
            />
          </Grid>

          <Divider />
          <Card>
            <Grid
              container
              direction="column"
              width="fit-content"
              alignContent="center"
              padding="20px"
            >
              <CustomSearchHeader />
              <CustomSearchForm onChange={setCustomSearchQuerry} />
              <Stack direction={'row'} spacing={1} sx={{ flexGrow: 1 }}>
                <LocationLink
                  to={`/search-results/${isGoldenOnly ? 'golden' : 'patient'}`}
                  search={{ payload: customSearchQuerry }}
                  style={{ textDecoration: 'none' }}
                >
                  <Button variant="contained">Search</Button>
                </LocationLink>
                <Button variant="outlined" href="/search/simple">
                  Cancel
                </Button>
              </Stack>
            </Grid>
          </Card>
        </Grid>
      </Container>
    </>
  )
}

export default CustomSearch
