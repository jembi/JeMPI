import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import UniqueToInteraction from 'pages/settings/uniqueToInteraction/UniqueToInteraction';


describe('UniqueToInteraction', () => {
  const uniqueGoldenRecordFieldsWithIds = mockData.configuration.uniqueGoldenRecordFields.map((row, index) => ({
    ...row,
    id: `row_${index}`, 
  }));
  it('renders without crashing', () => {
    render(<UniqueToInteraction uniqueInteractionData={uniqueGoldenRecordFieldsWithIds} />);
  });

  it('handles edit mode', async () => {
    
    render(<UniqueToInteraction uniqueInteractionData={uniqueGoldenRecordFieldsWithIds} />);
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