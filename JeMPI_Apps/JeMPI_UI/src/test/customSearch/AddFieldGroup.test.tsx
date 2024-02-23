import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CustomSearch from '../../components/customSearch/CustomSearch'
import { AppConfigProvider } from '../../hooks/useAppConfig'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

test.skip('Add group button successfully adds a new group when clicked', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <AppConfigProvider>
        <CustomSearch></CustomSearch>
      </AppConfigProvider>
    </QueryClientProvider>
  )

  const addGroupButton = await screen.findByRole('button', {
    name: 'Add group'
  })

  expect(addGroupButton).toBeInTheDocument()

  await act(async () => {
    userEvent.click(
      await screen.findByRole('button', {
        name: 'Add group'
      })
    )
  })

  const addedGroupButton = await screen.findAllByRole('button', {
    name: 'Add Search Rule'
  })

  expect(addedGroupButton).toHaveLength(2)
})
