import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NotificationWorklist from 'components/notificationWorklist/NotificationWorklist'
import { AppConfigProvider } from 'hooks/useAppConfig'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

test('User is in the notification screen, and the search input works as expected', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <AppConfigProvider>
        <NotificationWorklist />
      </AppConfigProvider>
    </QueryClientProvider>
  )
  const headerElement = await screen.findByText(/Notification Worklist/i)
  expect(headerElement).toBeInTheDocument()

  const searchInput = screen.getByPlaceholderText(/Filter/i)

  userEvent.type(searchInput, 'Golden record changed')

  const searchResult = await screen.findAllByText(/Review /i)

  await waitFor(() => expect(searchResult[0]).not.toBeInTheDocument(), {
    timeout: 500
  })
})
