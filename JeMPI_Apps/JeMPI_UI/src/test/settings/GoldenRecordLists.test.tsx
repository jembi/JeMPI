
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import GoldenRecordLists from 'pages/settings/goldenRecordLists/GoldenRecordLists';
import { useConfiguration } from 'hooks/useUIConfiguration';

jest.mock('hooks/useUIConfiguration', () => ({
  useConfiguration: jest.fn(),
}));

describe('GoldenRecordLists', () => {
  const mockConfiguration = mockData.configuration.additionalNodes;

  beforeEach(() => {
    (useConfiguration as jest.Mock).mockReturnValue({
      configuration: mockConfiguration,
      setConfiguration: jest.fn(),
    });
  });
  it('renders without crashing', () => {
    render(<GoldenRecordLists  />);
  });

  it('handles edit mode', async () => {
    render(<GoldenRecordLists />);
  
    const editIcon = await waitFor(() => document.getElementById('edit-button'));
    const saveButton = await waitFor(() => document.getElementById('save-button'));
    const cancelButton = await waitFor(() => document.getElementById('cancel-button'));
  
    if (saveButton && cancelButton && editIcon) {
      userEvent.click(editIcon);
      await waitFor(() => {
        expect(saveButton).toBeVisible();
        expect(cancelButton).toBeVisible();
      });
    }
  });

});