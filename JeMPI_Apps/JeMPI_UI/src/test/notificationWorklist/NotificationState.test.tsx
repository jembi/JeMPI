import React from 'react'
import { render } from '@testing-library/react'
import '@testing-library/jest-dom'
import { Typography } from '@mui/material'
import NotificationState from 'components/notificationWorklist/NotificationState'

describe('NotificationState Component', () => {
  const renderComponent = (value: string) => {
    return render(<NotificationState value={value} />)
  }

  it('should render the correct text', () => {
    const { getByText } = renderComponent('New')
    expect(getByText('New')).toBeInTheDocument()
  })

  it('should apply the correct color for New', () => {
    const { getByText } = renderComponent('New')
    expect(getByText('New')).toHaveStyle('color: rgb(25, 118, 210)')
  })
})
