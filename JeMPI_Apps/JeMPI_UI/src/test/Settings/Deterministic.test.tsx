import { render, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom/extend-expect'
import { DemographicField } from 'types/Configuration'
import Deterministic from 'pages/settings/deterministic/deterministic'

const mockDemographicData: DemographicField[] = [
  {
    fieldName: 'given_name',
    fieldType: 'String',
    source: 'Column 1',
    indexGoldenRecord: '@index(exact,trigram)',
    indexInteraction: '@index(exact,trigram)',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.8806329,
      u: 0.0026558
    }
  },
  {
    fieldName: 'family_name',
    fieldType: 'String',
    source: 'Column 2',
    indexGoldenRecord: '@index(exact,trigram)',
    indexInteraction: '@index(exact,trigram)',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.9140443,
      u: 0.0006275
    }
  },
  {
    fieldName: 'gender',
    fieldType: 'String',
    source: 'Column 3',
    indexGoldenRecord: '@index(exact,trigram)',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.9468393,
      u: 0.4436446
    }
  },
  {
    fieldName: 'dob',
    fieldType: 'String',
    source: 'Column 4',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.7856196,
      u: 0.0000465
    }
  },
  {
    fieldName: 'city',
    fieldType: 'String',
    source: 'Column 5',
    indexGoldenRecord: '@index(trigram)',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.8445694,
      u: 0.0355741
    }
  },
  {
    fieldName: 'phone_number',
    fieldType: 'String',
    source: 'Column 6',
    indexGoldenRecord: '@index(exact,trigram)',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.84085,
      u: 0.0000004
    }
  },
  {
    fieldName: 'national_id',
    fieldType: 'String',
    source: 'Column 7',
    indexGoldenRecord: '@index(exact,trigram)',
    indexInteraction: '@index(exact,trigram)',
    linkMetaData: {
      comparison: 'JARO_WINKLER_SIMILARITY',
      comparisonLevels: [0.92],
      m: 0.8441029,
      u: 0.0000002
    }
  }
]

describe('Deterministic Component', () => {
  test('renders correctly', () => {
    const { getByText, getByLabelText } = render(
      <Deterministic demographicData={mockDemographicData} />
    )

    expect(getByText('Design View')).toBeInTheDocument()
    expect(getByText('Source View')).toBeInTheDocument()
    expect(getByLabelText('Select Comparator Function')).toBeInTheDocument()
    expect(getByLabelText('Select Field')).toBeInTheDocument()
    expect(getByLabelText('Select Operator')).toBeInTheDocument()
  })

  test('changes viewType on button click', () => {
    const { getByText, queryByText } = render(
      <Deterministic demographicData={mockDemographicData} />
    )
    expect(queryByText('eq (National ID)')).not.toBeInTheDocument()
    fireEvent.click(getByText('Source View'))
    expect(getByText('eq (National ID)')).toBeInTheDocument()
  })

  test('handles comparator change', () => {
    const { getByLabelText } = render(
      <Deterministic demographicData={mockDemographicData} />
    )
    const selectComparator = getByLabelText(
      'Select Comparator Function'
    ) as HTMLSelectElement
    const changeEvent = new Event('change', { bubbles: true })
    Object.defineProperty(selectComparator, 'value', {
      writable: true,
      value: '1'
    })
    fireEvent(selectComparator, changeEvent)
    expect(selectComparator.value).toBe('1')
  })

  test('handles field change', () => {
    const { getByLabelText } = render(
      <Deterministic demographicData={mockDemographicData} />
    )
    const selectField = getByLabelText('Select Field') as HTMLSelectElement
    selectField.value = 'family_name'
    fireEvent.change(selectField)
    expect(selectField.value).toBe('family_name')
  })

  test('handles operator change', () => {
    const { getByLabelText } = render(
      <Deterministic demographicData={mockDemographicData} />
    )
    const selectOperator = getByLabelText(
      'Select Operator'
    ) as HTMLSelectElement

    selectOperator.value = 'And'
    fireEvent.change(selectOperator)
    expect(selectOperator.value).toBe('And')
  })
})
