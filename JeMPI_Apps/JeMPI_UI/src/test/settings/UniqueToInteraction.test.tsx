import { render, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import mockData from 'services/mockData'
import '@testing-library/jest-dom'
import UniqueToInteraction from 'pages/settings/uniqueToInteraction/UniqueToInteraction'
import { useConfiguration } from 'hooks/useUIConfiguration'


jest.mock('hooks/useUIConfiguration', () => ({
  useConfiguration: jest.fn(),
}));

describe('UniqueToInteraction', () => {
  const mockConfiguration = mockData.configuration.additionalNodes;

  beforeEach(() => {
    (useConfiguration as jest.Mock).mockReturnValue({
      configuration: mockConfiguration,
      setConfiguration: jest.fn(),
    });
  });
  
  it('renders without crashing', () => {
    render(<UniqueToInteraction />)
  })

  it('handles edit mode', async () => {
    render(<UniqueToInteraction />)
    const editIcon = document.getElementById('edit-button')
    const saveButton = document.getElementById('save-button')
    const cancelButton = document.getElementById('cancel-button')

    if (saveButton && cancelButton && editIcon) {
      userEvent.click(editIcon)
      await waitFor(() => {
        expect(saveButton).toBeVisible()
        expect(cancelButton).toBeVisible()
      })
    }
  })
})
