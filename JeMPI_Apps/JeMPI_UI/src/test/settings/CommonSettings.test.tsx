
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CommonSettings from 'pages/settings/common/Common';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import { useConfiguration } from 'hooks/useUIConfiguration';

jest.mock('hooks/useUIConfiguration', () => ({
  useConfiguration: jest.fn(),
}));


describe('CommonSettings Component', () => {
  const configData = mockData.configuration.demographicFields.map((row, index) => ({
    ...row,
    id: `row_${index}`, 
  }));
  beforeEach(() => {
    (useConfiguration as jest.Mock).mockReturnValue({
      configuration: configData,
      setConfiguration: jest.fn(),
    });
  });
  it('renders without crashing', () => {
    render(<CommonSettings  />);
  });

  it('handles edit mode', async () => {
    
    render(<CommonSettings  />);
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
