import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import { Card, CardContent, Container, Typography } from '@mui/material'
import PageHeader from '../shell/PageHeader'
import DropZone from './DropZone'

const Import = () => {
  return (
    <Container>
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
      <Card variant="outlined" sx={{ minWidth: 360, marginTop: '5px' }}>
        <CardContent>
          <Typography variant="h6" color="rgba(0, 0, 0, 0.6)">
            Bulk Upload
          </Typography>
          <DropZone />
        </CardContent>
      </Card>
    </Container>
  )
}

export default Import
