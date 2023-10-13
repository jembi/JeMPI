import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import { Container, Divider, Stack } from '@mui/material'
import PageHeader from '../shell/PageHeader'
import DropZone from './DropZone'

const Import = () => {
  return (
    <Container maxWidth={false}>
      <PageHeader
        title={'Import'}
        breadcrumbs={[
          {
            icon: <UploadFileIcon />,
            link: '/import/',
            title: 'Import'
          }
        ]}
        description={'Import or submit Patient records to MPI'}
      />
      <Divider />
      <Stack padding={{ lg: '1rem 1rem 1rem 1rem' }} sx={{ width: '100%' }}>
        <DropZone />
      </Stack>
    </Container>
  )
}

export default Import
