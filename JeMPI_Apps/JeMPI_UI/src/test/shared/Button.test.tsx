import { render, screen } from '@testing-library/react'
import Button from 'components/shared/Button'

test('Render Button Component', () => {
  render(<Button>Search</Button>)
  const linkElement = screen.getAllByText(/Search/i)
  expect(linkElement[0]).toBeInTheDocument()
})

test('Add full-width className when isFullWidth is true', () => {
  const { container } = render(<Button isFullWidth={true}>Search</Button>)
  expect(container.firstChild).toHaveClass('MuiButtonBase-root')
})

test('Show Circular progress icon when isLoading is true', () => {
  render(<Button isLoading={true}>Test</Button>)

  const { container } = render(<Button isLoading={true}>Search</Button>)
  expect(
    container.getElementsByClassName('MuiCircularProgress-circle')
  ).toBeTruthy()
})
