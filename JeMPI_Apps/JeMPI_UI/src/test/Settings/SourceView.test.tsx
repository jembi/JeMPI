import '@testing-library/jest-dom/extend-expect'
import { Source } from '@mui/icons-material'
import { render } from '@testing-library/react'

describe('Source Component', () => {
  test('it should mount', () => {
    const { container } = render(<Source data-testid="source-component" />)
    const sourceComponent = container.querySelector(
      '[data-testid="source-component"]'
    )
    expect(sourceComponent).toBeInTheDocument()
  })

  test('it should render initial state correctly', async () => {
    render(<Source data-testid="source-component" />)

    const addRuleButton = document.getElementById('add-rule-button')

    if (addRuleButton) expect(addRuleButton).toBeVisible()

    const fieldsSelect = document.querySelector(`[aria-label="fields"]`)

    if (fieldsSelect) expect(fieldsSelect).toBeVisible()

    const rulesBox = document.getElementById('rules-box')
    if (rulesBox) expect(rulesBox).toBeInTheDocument()

    const generatedRulesTitle = document.getElementsByName('generated rules')[0]
    if (generatedRulesTitle) expect(generatedRulesTitle).toBeInTheDocument()
  })
})
