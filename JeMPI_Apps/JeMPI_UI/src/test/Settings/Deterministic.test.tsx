import { render, fireEvent, screen } from '@testing-library/react';
import { DemographicField } from 'types/Configuration';
import userEvent from '@testing-library/user-event';
import Deterministic from 'pages/settings/deterministic/deterministic';

const mockDemographicData: DemographicField[] = [
  { fieldName: 'name', fieldType: 'string' },
  { fieldName: 'age', fieldType: 'number' },
  { fieldName: 'gender', fieldType: 'string' },
];

describe('Deterministic Component', () => {
  it('renders correctly', () => {
    render(<Deterministic demographicData={mockDemographicData} />);
    expect(screen.getByText('Design View')).toBeInTheDocument();
    expect(screen.getByText('Source View')).toBeInTheDocument();
  });

  it('handles comparator change', () => {
    render(<Deterministic demographicData={mockDemographicData} />);
    const selectComparator = screen.getByLabelText('Select Comparator Function');
    userEvent.selectOptions(selectComparator, '1'); // or 'Low Fuzziness', 'Medium Fuzziness', or 'High Fuzziness'
    expect(screen.getByText('Low Fuzziness')).toBeInTheDocument();
  });

  it('handles field change', () => {
    render(<Deterministic demographicData={mockDemographicData} />);
    const selectField = screen.getByLabelText('Select Field');
    fireEvent.change(selectField, { target: { value: 'age' } });
    expect(screen.getByText('age')).toBeInTheDocument();
  });

  it('handles operator change', () => {
    render(<Deterministic demographicData={mockDemographicData} />);
    const selectOperator = screen.getByLabelText('Select Operator');
    fireEvent.change(selectOperator, { target: { value: 'And' } });
    expect(screen.getByText('And')).toBeInTheDocument();
  });

  it('saves state to localStorage', () => {
    render(<Deterministic demographicData={mockDemographicData} />);
    const selectComparator = screen.getByLabelText('Select Comparator Function');
    const selectField = screen.getByLabelText('Select Field');
    const selectOperator = screen.getByLabelText('Select Operator');
    fireEvent.change(selectComparator, { target: { value: 1 } });
    fireEvent.change(selectField, { target: { value: 'age' } });
    fireEvent.change(selectOperator, { target: { value: 'And' } });
    expect(localStorage.getItem('selectedComparator')).toBe('1');
    expect(localStorage.getItem('selectedField')).toBe('age');
    expect(localStorage.getItem('selectedOperator')).toBe('And');
  });
});