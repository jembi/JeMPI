import { render, screen } from '@testing-library/react'
import SearchtextInput from '../../components/search/SearchTextInput'

test('Text field renders successfully when called', () => {
  render(
    <SearchtextInput
      name="test"
      value="Test input"
      label="Test label"
      size="small"
      index={1}
      sx={{ width: '20px' }}
    />
  )

  const textbox = screen.getByRole('textbox')
  expect(textbox).toHaveValue('Test input')
})

test('Text field is disabled when the prop is true', () => {
  render(
    <SearchtextInput
      name="test"
      value="Test input"
      label="Test label"
      size="small"
      index={1}
      sx={{ width: '20px' }}
      disabled={true}
    />
  )

  const textbox = screen.getByRole('textbox')

  expect(textbox).toBeDisabled()
})
