import { Container, Divider, Stack } from '@mui/material'
import PageHeader from 'components/shell/PageHeader'
import React from 'react'

const Dashboard = () => {
  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Dashboard'}
        breadcrumbs={[
          {
            link: '/',
            title: 'Dashboard'
          }
        ]}
        description={''}
      />
      <Divider />
      <Stack padding={'2rem 1rem 1rem 1rem'}></Stack>
    </Container>
  )
}

export default Dashboard
