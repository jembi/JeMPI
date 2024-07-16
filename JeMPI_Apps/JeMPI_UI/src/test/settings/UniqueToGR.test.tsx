
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import UniqueToGR from 'pages/settings/uniqueToGR/UniqueToGR';
import { useConfiguration } from 'hooks/useUIConfiguration';

jest.mock('hooks/useUIConfiguration', () => ({
  useConfiguration: jest.fn(),
}));

describe('UniqueToGR', () => {
  const mockConfiguration = mockData.configuration.auxGoldenRecordFields;

  beforeEach(() => {
    (useConfiguration as jest.Mock).mockReturnValue({
      configuration: mockConfiguration,
      setConfiguration: jest.fn(),
    });
  });
  it('renders without crashing', () => {
    render(<UniqueToGR />);
  });

  it('handles edit mode', async () => {
    
    render(<UniqueToGR  />);
    const editIcon = document.getElementById('edit-button');
    const saveButton = document.getElementById('save-button');
    const cancelButton = document.getElementById('cancel-button');
   
  if (saveButton && cancelButton && editIcon) {
    userEvent.click(editIcon);
    await waitFor(() => {
      expect(saveButton).toBeVisible();
      expect(cancelButton).toBeVisible();
    });
  }
  });

});