import { MoreHorizOutlined } from '@mui/icons-material'
import SearchIcon from '@mui/icons-material/Search'
import { Card, Container, Grid } from '@mui/material'
import Divider from '@mui/material/Divider'
import { useState } from 'react'
import {
  FlagLabel,
  SearchQuery,
  ToggleButtonOptions
} from '../../types/SimpleSearch'
import Button from '../shared/Button'
import PageHeader from '../shell/PageHeader'
import SearchFlags from './SearchFlags'
import SimpleSearchForm from './SimpleSearchForm'
import SimpleSearchHeader from './SimpleSearchHeader'
import { useNavigate } from 'react-router-dom'

const SimpleSearch: React.FC = () => {
  const [isGoldenOnly, setIsGoldenOnly] = useState<boolean>(true)
  const [simpleSearchQuery, setSimpleSearchQuery] = useState<
    SearchQuery | undefined
  >(undefined)

  const options: ToggleButtonOptions[] = [
    { value: 0, label: FlagLabel.GOLDEN_ONLY },
    { value: 1, label: FlagLabel.PATIENT_ONLY }
  ]

  const navigate = useNavigate()
  return (
    <Container maxWidth={false}>
      <PageHeader
        description="Quickly access the information you need with our powerful search."
        title="Simple Search"
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
            href={'/search/custom'}
            size="large"
            key="custom-search"
          >
            CUSTOM SEARCH
          </Button>
        ]}
      />
      <Divider />
      <Card>
        <Grid
          container
          direction="column"
          width="fit-content"
          alignContent="center"
          padding="30px"
        >
          <SimpleSearchHeader isGoldenOnly={isGoldenOnly} />
          <SimpleSearchForm onChange={setSimpleSearchQuery} />
          <Grid item>
            <Button
              onClick={() =>
                navigate(
                  `/search-results/${isGoldenOnly ? 'golden' : 'patient'}`,
                  { state: { payload: simpleSearchQuery } }
                )
              }
              variant="contained"
            >
              Search
            </Button>
          </Grid>
        </Grid>
      </Card>
    </Container>
  )
}

export default SimpleSearch
