import {
  Alert,
  Box,
  Button,
  Container,
  Snackbar,
  Stack,
  Typography
} from '@mui/material'
import { Component, ErrorInfo, ReactNode } from 'react'

interface Props {
  children?: ReactNode
}

interface State {
  hasError: boolean
  error?: Error
  notify?: boolean
}

class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false
  }

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // eslint-disable-next-line no-console
    console.error('Uncaught error:', error, errorInfo)
  }

  public render() {
    if (this.state.hasError) {
      return (
        <Container>
          <Stack spacing={3}>
            <Typography variant="h2">
              Oops... It looks like something went wrong
            </Typography>
            <Box display="flex" alignItems="center" justifyContent="center">
              <span role="img" style={{ fontSize: 100 }}>
                🤯
              </span>
            </Box>
            <Typography variant="h4">
              Send this to your administrator
            </Typography>
            <Typography>{this.state.error?.message}</Typography>
            <Typography>{this.state.error?.stack}</Typography>
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Button
                onClick={() => {
                  navigator.clipboard.writeText(this.state.error?.stack || '')
                  this.setState({ ...this.state, notify: true })
                }}
                sx={{ width: 160 }}
              >
                Copy to clipboard
              </Button>
            </Box>
          </Stack>
          <Snackbar
            open={this.state.notify}
            autoHideDuration={2000}
            onClose={() => {
              this.setState({ ...this.state, notify: false })
            }}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
          >
            <Alert>Details successfully copied to clipboard</Alert>
          </Snackbar>
        </Container>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary
