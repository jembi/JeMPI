import { Container, Skeleton } from '@mui/material'

const Loading = () => {
  return (
    <Container>
      <Skeleton variant="text" height={64} width={'50%'}></Skeleton>
      <Skeleton variant="text" height={32} width={'40%'}></Skeleton>
      <Skeleton variant="rectangular" height={600} sx={{ mt: 2 }}></Skeleton>
    </Container>
  )
}

export default Loading
