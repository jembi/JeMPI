import { render, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom/extend-expect'
import SourceView, { RowData } from 'pages/settings/deterministic/SourceView'

describe('SourceView Component', () => {
  const mockData: RowData[] = [
    { id: 1, ruleNumber: 101, ruleText: 'eq(national_id)' },
    {
      id: 2,
      ruleNumber: 102,
      ruleText: 'eq(given_name) and eq(family_name) and eq(phone_number)'
    }
  ]
  const mockOnEditRow = jest.fn()

  it('renders the component with initial data', () => {
    render(
      <SourceView
        data={mockData}
        onEditRow={mockOnEditRow}
        onAddUndefinedRule={()=> { return null }}
        hasUndefinedRule={false}
      />
    )
    expect(document.body).toHaveTextContent('eq(national_id)')
    expect(document.body).toHaveTextContent(
      'eq(given_name) and eq(family_name) and eq(phone_number)'
    )
  })

  it('edits a row', async () => {
    render(
      <SourceView
        data={mockData}
        onEditRow={mockOnEditRow}
        onAddUndefinedRule={()=> { return null }}
        hasUndefinedRule={false}
      />
    )
    fireEvent.click(document.getElementById('edit-button-1')!)
    expect(mockOnEditRow).toHaveBeenCalledWith({
      id: 1,
      ruleNumber: 101,
      ruleText: 'eq(national_id)'
    })
  })
})
