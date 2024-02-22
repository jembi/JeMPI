import { act, render, screen } from '@testing-library/react'
import SearchRowTest from './SearchRow'

test.skip('Field group renders successfully when called', async () => {
  await act(() => {
    render(<SearchRowTest />)
  })
  const element = screen.queryByText('Or')
  expect(element).not.toBeInTheDocument()

  //Remove group button should not be visible when fieldGroupIndex is greater than 0
  const removeGroupButton = screen.queryByRole('button', {
    name: 'Remove group'
  })

  expect(removeGroupButton).not.toBeInTheDocument()
})
