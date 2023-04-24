import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SearchRowTest from './SearchRow'

test('Delete button show when we have more than one row and it deletes search rule successfully', async () => {
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

  const DeleteSearchRule = await screen.findByTestId('DeleteIcon')
  expect(DeleteSearchRule).toBeInTheDocument()

  await act(async () => {
    userEvent.click(await screen.findByTestId('DeleteIcon'))
  })

  const DeletedSearchRule = screen.queryByTestId('DeleteIcon')
  expect(DeletedSearchRule).not.toBeInTheDocument()
})
