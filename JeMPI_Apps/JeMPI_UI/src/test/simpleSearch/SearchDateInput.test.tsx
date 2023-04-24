import { render, screen } from '@testing-library/react'
import SearchDateInput from '../../components/search/SearchDateInput'

test('Show correct label when rendered', () => {
  const { container } = render(
    <SearchDateInput
      name="test"
      value="1/01/2023"
      label="Test date"
      size="small"
      isCustomRow={true}
      index={1}
      sx={{ width: '20px' }}
    />
  )
  expect(container.firstChild).toHaveTextContent('Test date')
})

test('Show correct date value when selected with date picker', () => {
  render(
    <SearchDateInput
      name="test"
      value="1/01/2023"
      label="Test date"
      size="small"
      isCustomRow={true}
      index={1}
      sx={{ width: '20px' }}
    />
  )

  const date = screen.getAllByRole('textbox')
  expect(date[0]).toHaveValue('01/01/2023')
})

test('Change textbox color when input the wrong date format', () => {
  const { container } = render(
    <SearchDateInput
      name="test"
      value="1/01/2023"
      label="Test date"
      size="small"
      isCustomRow={true}
      index={1}
      sx={{ width: '20px' }}
    />
  )

  expect(container?.querySelector('input')?.getAttribute('aria-invalid')).toBe(
    'false'
  )
})
