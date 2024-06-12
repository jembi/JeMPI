
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import GoldenRecordLists from 'pages/settings/goldenRecordLists/GoldenRecordLists';


describe('GoldenRecordLists', () => {
  const goldenRecordListsWithIds = mockData.configuration.auxGoldenRecordFields.map((row, index) => ({
    ...row,
    id: `row_${index}`, 
  }));
  it('renders without crashing', () => {
    render(<GoldenRecordLists goldenRecordList={goldenRecordListsWithIds} />);
  });

  it('handles edit mode', async () => {
    render(<GoldenRecordLists goldenRecordList={goldenRecordListsWithIds} />);
  
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