import { render, getByRole, getByText } from '@testing-library/react';
import NotFound from 'components/error/NotFound';

describe('NotFound component', () => {
  it('renders correctly', () => {
    const { container } = render(<NotFound />);
    expect(getByRole(container, 'heading', { name: 'Well this is awkward...' })).toBeInTheDocument();
    expect(getByText(container, '404')).toBeInTheDocument();
    expect(getByRole(container, 'heading', { name: 'Maybe you\'re looking for a page we haven\'t built yet' })).toBeInTheDocument();
  });
});