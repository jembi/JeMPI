import { Container, Stack, Typography } from '@mui/material'

const NotFound = () => {
  return (
    <Container>
      <Stack spacing={3} alignItems="center">
        <Typography variant="h2">Well this is awkward... </Typography>
        <span role="img" style={{ fontSize: 140 }}>
          🤔
        </span>
        <Typography variant="h1" fontSize={200}>
          404
        </Typography>
        <Typography variant="h3">
          Maybe you&aposre looking for a page we haven&apost built yet
        </Typography>
      </Stack>
    </Container>
  )
}

export default NotFound
