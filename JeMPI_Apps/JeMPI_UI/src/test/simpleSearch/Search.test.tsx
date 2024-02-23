import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SimpleSearch from '../../components/search/SimpleSearch'
import { AppConfigProvider } from '../../hooks/useAppConfig'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

test.skip('Simple Search button exist when we navigate to custom search', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <AppConfigProvider>
        <SimpleSearch />
      </AppConfigProvider>
    </QueryClientProvider>
  )
  const linkElement = await screen.findAllByText(/Custom Search/i)
  expect(linkElement[0]).toBeInTheDocument()

  const customSearchButton = await screen.findAllByRole('link', {
    name: /Custom Search/i
  })

  act(() => {
    userEvent.click(customSearchButton[0])
  })

  expect(await screen.findByText(/Simple Search/i)).toBeInTheDocument()
})
