
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CommonSettings from 'pages/settings/common/Common';
import mockData from 'services/mockData';
import '@testing-library/jest-dom';
import { useConfiguration } from 'hooks/useUIConfiguration';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider } from 'hooks/useConfig';
import { BrowserRouter } from 'react-router-dom';

jest.mock('hooks/useUIConfiguration', () => ({
  useConfiguration: jest.fn(),
}));


const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

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
    render(<QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ConfigProvider>
        <CommonSettings  />
        </ConfigProvider>
      </BrowserRouter>
    </QueryClientProvider>);
  });

  it('handles edit mode', async () => {
    
    render(
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <ConfigProvider>
            <CommonSettings  />
            </ConfigProvider>
          </BrowserRouter>
        </QueryClientProvider>
      )
    
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
