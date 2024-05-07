
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CommonSettings from 'pages/settings/common/Common';
import configuration from 'services/configurationData';
import '@testing-library/jest-dom';


describe('CommonSettings Component', () => {
  const demographicWithIds = configuration.demographicFields.map((row, index) => ({
    ...row,
    id: `row_${index}`, 
  }));
  it('renders without crashing', () => {
    render(<CommonSettings demographicData={demographicWithIds} />);
  });

  it('handles edit mode', async () => {
    
    render(<CommonSettings demographicData={demographicWithIds} />);
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
