import { MoreHorizOutlined } from '@mui/icons-material'
import SearchIcon from '@mui/icons-material/Search'
import { Card, Container, Grid } from '@mui/material'
import Divider from '@mui/material/Divider'
import { Link as LocationLink } from '@tanstack/react-location'
import { useState } from 'react'
import {
  FlagLabel,
  SearchQuery,
  ToggleButtonOptions
} from '../../types/SimpleSearch'
import PageHeader from '../shell/PageHeader'
import SearchFlags from '../search/SearchFlags'
import SimpleSearchForm from '../search/SimpleSearchForm'
import SimpleSearchHeader from '../search/SimpleSearchHeader'
import { SxProps, TextField, Theme } from '@mui/material'
import apiClient from "../../services/ApiClient";
import { Link } from 'react-router-dom';
const NotificationListFilter: React.FC = () => {
  const [limit, setLimit] = useState('');
  const [offset, setOffset] = useState('');
  const [created, setCreated] = useState('');
  const handleClick = async () => {
    const matches = await apiClient.getMatches(limit, offset, created);
    console.log(matches);
  };

    const handleSubmit = () => {
      // Navigate to the next page
      const url = `/notifications/notification-list?limit=${limit}&offset=${offset}&created=${created}`;
      window.open(url, '_blank');
    };
  return (
    <Container maxWidth={false}>
      <PageHeader
        description="Use Date, Limit and Offset To Limit Number of Notifications."
        title="Notifications Filter"
        breadcrumbs={[
          {
            icon: <MoreHorizOutlined />
          },
          {
            icon: <SearchIcon />,
            title: 'Filter'
          }
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
        <TextField
               placeholder="Limit"
               id="limit"
               label="Limit"
               variant="outlined"
               name="Limit"
               value={limit}
               onChange={(e) => setLimit(e.target.value)}
         />
         <TextField
               placeholder="Offset"
               id="Offset"
               label="Offset"
               variant="outlined"
               name="Offset"
               value={offset}
               onChange={(e) => setOffset(e.target.value)}
          />
          <TextField
               placeholder="date created"
               id="created"
               label="date created"
               variant="outlined"
               name="created"
               value={created}
               onChange={(e) => setCreated(e.target.value)}
          />
            <input type="submit" onClick={handleSubmit} />
        </Grid>
      </Card>
    </Container>
  )
}

export default NotificationListFilter
