import { act, render, screen } from '@testing-library/react'
import App from './App'

test('renders JeMPI logo', async () => {
  await act(() => {
    render(<App />)
  })
  const linkElement = await screen.findAllByText(/MPI/i)
  expect(linkElement[0]).toBeInTheDocument()
})
