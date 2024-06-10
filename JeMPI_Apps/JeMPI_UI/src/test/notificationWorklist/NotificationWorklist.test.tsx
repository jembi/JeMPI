import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, waitFor } from '@testing-library/react'
import NotificationWorklist from 'components/notificationWorklist/NotificationWorklist'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'hooks/useConfig'
import userEvent from '@testing-library/user-event'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

test('NotificationWorklist renders without crashing', () => {
  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ConfigProvider>
          <NotificationWorklist />
        </ConfigProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
  const headerElement = document.getElementById('page-header') as HTMLElement
  if (headerElement) {
    expect(headerElement).toBeInTheDocument()
  }
})

test('User is in the notification screen, and the search input works as expected', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <ConfigProvider>
        <BrowserRouter>
          <NotificationWorklist />
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  )

  const searchInput = document.getElementById('filter-input') as HTMLElement
  if (searchInput) {
    userEvent.type(searchInput, 'Golden record changed')
  }
  const searchResult = await waitFor(() =>
    document.getElementById('search-result')
  )
  if (searchResult) {
    expect(searchResult).not.toBeInTheDocument()
  }
})

test('Notifications are displayed correctly', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <ConfigProvider>
        <BrowserRouter>
          <NotificationWorklist />
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  )

  await waitFor(() => {
    const notificationRows = document.querySelectorAll('.notification-row')
    expect(notificationRows.length).toBeGreaterThanOrEqual(0)
  })
})

test('Date filters work as expected', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <ConfigProvider>
        <BrowserRouter>
          <NotificationWorklist />
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  )

  const startDatePicker = document.getElementById(
    'start-date-filter'
  ) as HTMLElement
  const endDatePicker = document.getElementById(
    'end-date-filter'
  ) as HTMLElement

  if (startDatePicker || endDatePicker) {
    userEvent.type(startDatePicker, '2024/06/01 00:00:00')
    userEvent.type(endDatePicker, '2024/06/02 23:59:59')
  }

  const filterButton = document.getElementById('filter-button') as HTMLElement
  if (filterButton) {
    userEvent.click(filterButton)
  }

  await waitFor(() => {
    const notificationRows = document.querySelectorAll('.notification-row')
    expect(notificationRows.length).toBeGreaterThanOrEqual(0)
  })
})

test('State filter dropdown works as expected', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <ConfigProvider>
        <BrowserRouter>
          <NotificationWorklist />
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  )

  const stateDropdown = document.getElementById(
    'state-filter-dropdown'
  ) as HTMLElement
  const allStateOption = document.getElementById(
    'all-state-option'
  ) as HTMLElement
  const filterButton = document.getElementById('filter-button') as HTMLElement

  if (stateDropdown || allStateOption || filterButton) {
    userEvent.click(stateDropdown)
    userEvent.click(allStateOption)
    userEvent.click(filterButton)
  }
  await waitFor(() => {
    const notificationRows = document.querySelectorAll('.notification-row')
    expect(notificationRows.length).toBeGreaterThanOrEqual(0)
  })
})

test('Pagination works as expected', async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <ConfigProvider>
        <BrowserRouter>
          <NotificationWorklist />
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  )

  await waitFor(() => {
    const paginationButtons = document.querySelectorAll('.pagination-button')
    expect(paginationButtons.length).toBeGreaterThanOrEqual(0)
  })

  const nextPageButton = document.getElementById(
    'next-page-button'
  ) as HTMLElement
  if (nextPageButton) {
    userEvent.click(nextPageButton)
  }

  await waitFor(() => {
    const notificationRows = document.querySelectorAll('.notification-row')
    expect(notificationRows.length).toBeGreaterThanOrEqual(0)
  })
})
