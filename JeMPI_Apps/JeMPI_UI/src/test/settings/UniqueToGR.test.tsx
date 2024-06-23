
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import UniqueToGR from 'pages/settings/uniqueToGR/UniqueToGR';


describe('UniqueToGR', () => {
  const uniqueGoldenRecordFieldsWithIds = mockData.configuration.auxGoldenRecordFields.map((row, index) => ({
    ...row,
    id: `row_${index}`, 
  }));
  it('renders without crashing', () => {
    render(<UniqueToGR uniqueToGoldenRecordData={uniqueGoldenRecordFieldsWithIds} />);
  });

  it('handles edit mode', async () => {
    
    render(<UniqueToGR uniqueToGoldenRecordData={uniqueGoldenRecordFieldsWithIds} />);
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