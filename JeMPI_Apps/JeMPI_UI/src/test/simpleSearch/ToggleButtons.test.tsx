import { render, screen } from '@testing-library/react'
import ToggleButtons from '../../components/search/ToggleButtons'

const options = [
  { value: 0, label: '0' },
  { value: 1, label: '1' },
  { value: 2, label: '2' },
  { value: 3, label: '3' }
]

test('Have content 1 when passed as an option label', () => {
  const { container } = render(
    <ToggleButtons selectedButton="1" options={options} />
  )
  expect(container.firstChild).toHaveTextContent('1')
})

test('Have option one when passed as the initial selected value', () => {
  render(<ToggleButtons selectedButton="1" options={options} />)

  const linkElement = screen.getAllByText('1')
  expect(linkElement[0]).toHaveTextContent('1')
})
