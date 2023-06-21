import { Container, Stack, Typography } from '@mui/material'

const NotFound = () => {
  return (
    <Container>
      <Stack spacing={3} alignItems="center">
        <Typography variant="h2">Well this is awkward... </Typography>
        <span role="img" style={{ fontSize: 70 }}>
          🤔
        </span>
        <Typography variant="h1" fontSize={100}>
          404
        </Typography>
        <Typography variant="h3" fontSize={50}>
          Maybe you&apos;re looking for a page we haven&apos;t built yet
        </Typography>
      </Stack>
    </Container>
  )
}

export default NotFound
