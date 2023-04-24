import { ReactLocation, Route, Router } from '@tanstack/react-location'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CustomSearch from '../../components/customSearch/CustomSearch'
import SimpleSearch from '../../components/search/SimpleSearch'
import SearchResult from '../../components/searchResult/SearchResult'
import { AppConfigProvider } from '../../hooks/useAppConfig'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})
const location = new ReactLocation()
const routes: Route[] = [
  {
    path: '',
    children: [
      {
        path: 'search',
        children: [
          {
            path: 'simple',
            element: <SimpleSearch />
          },
          {
            path: 'custom',
            element: <CustomSearch />
          }
        ]
      },
      {
        path: 'search-results',
        children: [
          {
            path: 'golden',
            element: (
              <SearchResult isGoldenRecord={true} title="Golden Records Only" />
            )
          },
          {
            path: 'patient',
            element: (
              <SearchResult
                isGoldenRecord={false}
                title="Patient Records Only"
              />
            )
          }
        ]
      }
    ]
  }
]

test('Simple Search button exist when we navigate to custom search', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <Router location={location} routes={routes}>
        <AppConfigProvider>
          <SimpleSearch />
        </AppConfigProvider>
      </Router>
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
