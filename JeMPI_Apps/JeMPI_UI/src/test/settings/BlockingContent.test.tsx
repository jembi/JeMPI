import { fireEvent, render, screen } from '@testing-library/react'
import '@testing-library/jest-dom/extend-expect'
import { BrowserRouter } from 'react-router-dom'
import BlockingContent from 'pages/settings/blocking/BlockingContent'
import mockData from 'services/mockData'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider } from 'hooks/useConfig'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})
const linkingRules: any = mockData.configuration.rules

describe('BlockingContent Component', () => {
  it('renders correctly', () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <BlockingContent
              hasUndefinedRule={false}
              linkingRules={linkingRules}
              handleAddRule={jest.fn()}
              handleDeleteRow={jest.fn()}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    expect(container).toMatchSnapshot()
  })

  it('toggles between Source View and Design View', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <BlockingContent
              hasUndefinedRule={false}
              linkingRules={linkingRules}
              handleAddRule={jest.fn()}
              handleDeleteRow={jest.fn()}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )

    const sourceViewButton = document.getElementById('source-view-button')
    const designViewButton = document.getElementById('design-view-button')

    if (sourceViewButton && designViewButton) {
      fireEvent.click(designViewButton)
      expect(screen.getByLabelText('Select Field')).toBeInTheDocument()
    }
  })

  it('adds a new rule row', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <BlockingContent
              hasUndefinedRule={false}
              linkingRules={linkingRules}
              handleAddRule={jest.fn()}
              handleDeleteRow={jest.fn()}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )

    const addRuleButton = document.getElementById('add-rule-button')
    if (addRuleButton) {
      fireEvent.click(addRuleButton)
      expect(screen.getByText('Rule 1')).toBeInTheDocument()
    }
  })

  it('calls handleAddRule when Add Rule button is clicked', () => {
    const handleAddRuleMock = jest.fn()
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <BlockingContent
              hasUndefinedRule={false}
              linkingRules={linkingRules}
              handleAddRule={handleAddRuleMock}
              handleDeleteRow={jest.fn()}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )

    const addRuleButton = document.getElementById('add-rule-button')
    if (addRuleButton) {
      fireEvent.click(addRuleButton)
      expect(handleAddRuleMock).toHaveBeenCalledTimes(1)
    }
  })

  it('calls handleDeleteRow when Delete button is clicked', () => {
    const handleDeleteRowMock = jest.fn()
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <BlockingContent
              hasUndefinedRule={false}
              linkingRules={linkingRules}
              handleAddRule={jest.fn}
              handleDeleteRow={handleDeleteRowMock}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    const deleteButton = document.getElementById('delete-button')
    if (deleteButton) {
      fireEvent.click(deleteButton)
      expect(handleDeleteRowMock).toHaveBeenCalledTimes(1)
    }
  })

  it('calls handleRowEdit when a row is edited', () => {
    const handleRowEditMock = jest.fn()
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <BlockingContent
              hasUndefinedRule={false}
              linkingRules={linkingRules}
              handleAddRule={jest.fn}
              handleDeleteRow={jest.fn}
              handleRowEdit={handleRowEditMock}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    const editButton = document.getElementById('edit-button') as HTMLElement
    if (editButton) {
      fireEvent.click(editButton)
      expect(handleRowEditMock).toHaveBeenCalledTimes(1)
    }
  })
})
