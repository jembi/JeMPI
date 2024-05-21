import { render, screen } from '@testing-library/react';
import ApiErrorMessage from 'components/error/ApiErrorMessage';


describe('ApiErrorMessage component', () => {

  it('renders error message', () => {
    const error = {
      message: 'Test error message',
      isAxiosError: true,
      toJSON: () => ({}),
      name: 'Error',
    };
    
    render(<ApiErrorMessage error={error} />);
    expect(screen.getByText('Error')).toBeInTheDocument();
    expect(screen.getByText(error.message)).toBeInTheDocument();
  });

  it('renders default error message when no error is provided', () => {
    render(<ApiErrorMessage />);
    expect(screen.getByText('Error')).toBeInTheDocument();
    expect(screen.getByText('ERROR')).toBeInTheDocument();
  });

  it('renders no error message when error is null', () => {
    render(<ApiErrorMessage error={null} />);
    expect(screen.queryByText('Error')).not.toBeInTheDocument();
  });
});
