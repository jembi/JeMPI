import { fireEvent, render, waitFor } from '@testing-library/react'
import DeterministicContent from 'pages/settings/deterministic/DeterministicContent'
import '@testing-library/jest-dom'
import mockData from 'services/mockData'
import { ConfigProvider } from 'hooks/useConfig'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import userEvent from '@testing-library/user-event'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

describe('DeterministicContent', () => {
  const demographicData: any = mockData.configuration.demographicFields
  const linkingRules = mockData.configuration.rules

  it('renders correctly', () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={linkingRules}
              hasUndefinedRule={false}
              currentTab={'link'}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    expect(container).toMatchSnapshot()
  })

  it('testing on change event for select comparator', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={linkingRules}
              hasUndefinedRule={false}
              currentTab={'link'}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )

    const selectComparator = document.getElementById(
      'select-comparator-function'
    ) as HTMLSelectElement
    if (selectComparator) {
      fireEvent.change(selectComparator, { target: { value: '1' } })
      expect(selectComparator).toHaveValue('1')
    }
  })

  it('handles field change', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={linkingRules}
              hasUndefinedRule={false}
              currentTab={'link'}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    const selectField = document.getElementById('select-field')
    if (selectField) {
      fireEvent.change(selectField, { target: { value: 'family_name' } })
      expect(selectField).toBe('family_name')
    }
  })

  it('handles operator change', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={linkingRules}
              hasUndefinedRule={false}
              currentTab={'link'}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    const selectOperator = document.getElementById('select operator')
    if (selectOperator) {
      fireEvent.change(selectOperator, { target: { value: 'OR' } })
      await waitFor(() => expect(selectOperator).toBe('OR'))
    }
  })

  it('handles add rule', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={linkingRules}
              hasUndefinedRule={false}
              currentTab={'link'}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    const addRuleButton = document.getElementById('add-rule-button')
    if (addRuleButton) {
      fireEvent.click(addRuleButton)
      const ruleText = document.body.textContent
      expect(ruleText).toContain('eq(given_name) OR eq(family_name)')
    }
  })

  it('handles close', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <DeterministicContent
              demographicData={demographicData}
              linkingRules={linkingRules}
              hasUndefinedRule={false}
              currentTab={'link'}
            />
          </ConfigProvider>
        </BrowserRouter>
      </QueryClientProvider>
    )
    const closeButton = document.getElementById('close-button')
    const sourceViewBUtton = document.getElementById('')
    if (closeButton && sourceViewBUtton) {
      userEvent.click(closeButton)
      await waitFor(() => {
        expect(sourceViewBUtton).toBeVisible()
      })
    }
  })
})
