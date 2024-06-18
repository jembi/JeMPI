import { render, fireEvent, waitFor } from '@testing-library/react'
import DeterministicContent from 'pages/settings/deterministic/DeterministicContent'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom'
import mockData from 'services/mockData'

describe('DeterministicContent', () => {
  const demographicData: any = mockData.configuration.demographicFields

  const linkingRules = mockData.configuration.rules

  it('renders correctly', () => {
    const { container } = render(
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
        hasUndefinedRule={false}
      />
    )
    expect(container).toMatchSnapshot()
  })

  it('testing on change event for select comparator', () => {
    render(
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
        hasUndefinedRule={false}
      />
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
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
        hasUndefinedRule={false}
      />
    )
    const selectField = document.getElementById('select-field')
    if (selectField) {
      fireEvent.change(selectField, { target: { value: 'family_name' } })
      expect(selectField).toBe('family_name')
    }
  })

  it('handles operator change', async () => {
    render(
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
        hasUndefinedRule={false}
      />
    )
    const selectOperator = document.getElementById('select operator')
    if (selectOperator) {
      fireEvent.change(selectOperator, { target: { value: 'OR' } })
      await waitFor(() => expect(selectOperator).toBe('OR'))
    }
  })

  it('handles add rule', () => {
    render(
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
        hasUndefinedRule={false}
      />
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
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
        hasUndefinedRule={false}
      />
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
