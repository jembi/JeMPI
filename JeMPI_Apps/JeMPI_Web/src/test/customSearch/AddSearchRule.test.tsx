import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SearchRowTest from './SearchRow'

test('Search rule is successfully added when we click on add search rule button', async () => {
  await act(() => {
    render(<SearchRowTest />)
  })

  await act(async () => {
    userEvent.click(
      await screen.findByRole('button', {
        name: 'Add Search Rule'
      })
    )
  })

  const addSearchRule = await screen.findByText('And')
  expect(addSearchRule).toBeInTheDocument()
})
