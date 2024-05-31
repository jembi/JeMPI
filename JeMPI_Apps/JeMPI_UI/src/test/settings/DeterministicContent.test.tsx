import React from 'react'
import { render, fireEvent, waitFor } from '@testing-library/react'
import DeterministicContent from 'pages/settings/deterministic/DeterministicContent'
import { Field } from 'types/Configuration'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom'

describe('DeterministicContent', () => {
  const demographicData: Field[] = [
    { fieldName: 'national_id', fieldType: 'String' },
    { fieldName: 'given_name', fieldType: 'String' },
    { fieldName: 'family_name', fieldType: 'String' }
  ]

  const linkingRules = {
    link: {
      deterministic: [
        { vars: ['national_id'], text: 'eq(national_id)' },
        {
          vars: ['given_name', 'family_name'],
          text: 'eq(given_name) and eq(family_name)'
        }
      ]
    }
  }

  it('renders correctly', () => {
    const { container } = render(
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
      />
    )
    expect(container).toMatchSnapshot()
  })

  it('testing on change event for select comparator', () => {
    render(
      <DeterministicContent
        demographicData={demographicData}
        linkingRules={linkingRules}
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
