import { render, screen } from '@testing-library/react'

import AddFieldOrGroupButton from '../../components/customSearch/AddFieldOrGroupButton'

const onClick = () => {
  console.log('I was clicked')
}

test('Add field or Group button render successfully when called', () => {
  render(
    <AddFieldOrGroupButton
      label="Test Button"
      initialCustomSearchValues={{
        parameters: [
          {
            fieldName: 'firstName',
            value: 'Lumiere',
            distance: 1
          }
        ]
      }}
      onClick={onClick}
    />
  )

  const button = screen.getByRole('button', { name: 'Test Button' })
  expect(button).toBeInTheDocument()
})
