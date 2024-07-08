import { useState, useEffect } from 'react'
import {
  Box,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  IconButton
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import { Field, Rule } from 'types/Configuration'
import { transformFieldName } from 'utils/helpers'
import SourceView, { RowData } from './SourceView'
import { useConfiguration } from 'hooks/useUIConfiguration'

interface DeterministicContentProps {
  demographicData: Field[]
  hasUndefinedRule: boolean
  linkingRules: {
    link: {
      deterministic: Rule[]
    }
  },
  currentTab: 'link' | 'validate' | 'matchNotification'
}

export const options = [
  { value: 0, label: 'Exact' },
  { value: 1, label: 'Low Fuzziness' },
  { value: 2, label: 'Medium Fuzziness' },
  { value: 3, label: 'High Fuzziness' }
]

export enum Operator {
  AND = 'And',
  OR = 'Or'
}

const transformRulesToRowData = (rules: {
  link: { deterministic: Rule[] }
}): RowData[] => {
  return rules.link.deterministic.map((rule: Rule, index: number) => ({
    id: index,
    ruleNumber: index + 1,
    ruleText: rule.text,
    rowIndex: index
  }))
};

const DeterministicContent = ({
  demographicData = [],
  linkingRules,
  hasUndefinedRule,
  currentTab
}: DeterministicContentProps) => {
  const [viewType, setViewType] = useState<number>(0)
  const [comparators, setComparators] = useState<number[]>([])
  const [fields, setFields] = useState<string[]>([])
  const [operators, setOperators] = useState<Operator[]>([])
  const [rules, setRules] = useState<Rule[]>([])
  const [editedRowIndex, setEditedRowIndex] = useState<number>(0);
  const [initialState, setInitialState] = useState({
    comparators: [] as number[],
    fields: [] as string[],
    operators: [] as Operator[]
  })
  const { configuration, setConfiguration } = useConfiguration();
  const deterministicRules = transformRulesToRowData(linkingRules)

  useEffect(() => {
    const savedComparators = localStorage.getItem('comparators')
    const savedFields = localStorage.getItem('fields')
    const savedOperators = localStorage.getItem('operators')
    const savedRules = localStorage.getItem('rules')

    if (savedComparators || savedFields || savedOperators || savedRules) {
      const parsedRules = savedRules ? JSON.parse(savedRules) : []
      setRules(parsedRules)
      const parsedComparators = savedComparators
        ? JSON.parse(savedComparators)
        : []
      const parsedFields = savedFields ? JSON.parse(savedFields) : []
      const parsedOperators = savedOperators ? JSON.parse(savedOperators) : []
      setComparators(parsedComparators)
      setFields(parsedFields)
      setOperators(parsedOperators)
      setInitialState({
        comparators: parsedComparators,
        fields: parsedFields,
        operators: parsedOperators
      })
    }
  }, [])

  useEffect(() => {
    localStorage.setItem('comparators', JSON.stringify(comparators))
  }, [comparators])

  useEffect(() => {
    localStorage.setItem('fields', JSON.stringify(fields))
  }, [fields])

  useEffect(() => {
    localStorage.setItem('operators', JSON.stringify(operators))
  }, [operators])

  useEffect(() => {
    localStorage.setItem('rules', JSON.stringify(rules))
  }, [rules])

  const handleComparatorChange = (
    index: number,
    event: SelectChangeEvent<number>
  ) => {
    const newComparators = [...comparators]
    newComparators[index] = event.target.value as number
    setComparators(newComparators)
  }

  const handleUpdateConfiguration = (newRule: Rule) => {
    if (!configuration) return
    const updatedConfiguration = getUpdatedConfiguration(newRule)
    console.log('updated config', updatedConfiguration)
    if (updatedConfiguration) {
      setConfiguration(updatedConfiguration);
      localStorage.setItem('configuration', JSON.stringify(updatedConfiguration));
    }
  }

  const getUpdatedConfiguration = (newRule: Rule) => {
    if (!configuration) return;

    const updatedConfiguration = { ...configuration };

    switch (currentTab) {
      case 'link':
        if (!updatedConfiguration.rules.link.deterministic) {
          updatedConfiguration.rules.link.deterministic = [];
        }
        updatedConfiguration.rules.link.deterministic[editedRowIndex] = newRule;
        break;
      case 'validate':
        if (!updatedConfiguration.rules.validate.deterministic) {
          updatedConfiguration.rules.validate.deterministic = [];
        }
        updatedConfiguration.rules.validate.deterministic[editedRowIndex] = newRule;
        break;
      case 'matchNotification':
        if (!updatedConfiguration.rules.matchNotification.deterministic) {
          updatedConfiguration.rules.matchNotification.deterministic = [];
        }
        updatedConfiguration.rules.matchNotification.deterministic[editedRowIndex] = newRule;
        break;
      default:
        return;
    }

    return updatedConfiguration;
  };

  const handleFieldChange = (
    index: number,
    event: SelectChangeEvent<string>
  ) => {
    const newFields = [...fields]
    newFields[index] = event.target.value as string
    setFields(newFields)
  }

  const handleOperatorChange = (
    index: number,
    event: SelectChangeEvent<Operator>
  ) => {
    const newOperators = [...operators]
    newOperators[index] = event.target.value as Operator
    setOperators(newOperators)
  }

  const handleAddRule = () => {
    const vars = fields.filter(field => field !== '')
    const text = vars
      .map((field, index) => {
        const operator =
          index > 0 ? ` ${operators[index - 1].toLowerCase()} ` : ''
        const comparator = comparators[index]
        const comparatorFunction =
          comparator === 0 ? `eq(${field})` : `match(${field},${comparator})`
        return `${operator}${comparatorFunction}`
      })
      .join('')

    const rule: Rule = {
      vars,
      text
    }

    setRules([...rules, rule])
    handleUpdateConfiguration(rule)
    setInitialState({
      comparators: [...comparators],
      fields: [...fields],
      operators: [...operators]
    })
  }

  const handleRowEdit = (row: RowData) => {
    if (row.rowIndex !== undefined) {
      setEditedRowIndex(row.rowIndex);
    }

    const regex = /(eq|match)\(([^),]+)(?:, (\d+))?\)/g
    const matchedFields: string[] = []
    const matchedComparators: number[] = []
    let match

    while ((match = regex.exec(row.ruleText)) !== null) {
      matchedFields.push(match[2])
      matchedComparators.push(match[1] === 'eq' ? 0 : parseInt(match[3], 10))
    }

    setComparators(matchedComparators)
    setFields(matchedFields)
    setOperators(new Array(matchedFields.length - 1).fill(Operator.AND))
    setViewType(1)
  }

  const handleAddRow = () => {
    setComparators([...comparators, 0])
    setFields([...fields, ''])
    setOperators([...operators, Operator.AND])
  }

  const handleClose = () => {
    setViewType(0)
  }

  const isAddRuleDisabled = () => {
    if (
      fields.length === 0 ||
      fields.some(field => field.length === 0) ||
      operators.some(
        (operator, index) => index < fields.length - 1 && !operator
      )
    ) {
      return true
    }

    return (
      JSON.stringify(initialState) ===
      JSON.stringify({
        comparators,
        fields,
        operators
      })
    )
  }

  const handleAddUndefinedRule = () => {
    handleAddRow()
    setViewType(1)
  }

  return (
    <Box>
      <Box
        sx={{
          mr: 'auto',
          display: 'flex',
          flexDirection: 'row',
          gap: 2,
          marginBottom: '40px'
        }}
      >
        <Button
          id="source-view-button"
          variant="outlined"
          size="medium"
          onClick={() => setViewType(0)}
          disabled={viewType === 1}
        >
          Source View
        </Button>
        <Button
          id="design-view-button"
          variant="outlined"
          size="medium"
          onClick={() => setViewType(1)}
          disabled={viewType === 0}
        >
          Design View
        </Button>
      </Box>
      {viewType === 0 ? (
        <Box
          sx={{
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            gap: 2,
            justifyContent: 'center',
            alignItems: 'flex-start'
          }}
        >
          <SourceView
            data={deterministicRules}
            onEditRow={handleRowEdit}
            onAddUndefinedRule={handleAddUndefinedRule}
            hasUndefinedRule={hasUndefinedRule}
          />
        </Box>
      ) : (
        <>
          {fields.map((field, index) => (
            <Box
              key={index}
              sx={{
                display: 'flex',
                flexDirection: 'row',
                width: '100%',
                justifyContent: 'center',
                gap: 2,
                marginTop: '20px',
                marginBottom: '20px'
              }}
            >
              <FormControl fullWidth>
                <InputLabel id={`select-operator-label-${index}`}>
                  Select Operator
                </InputLabel>
                <Select
                  labelId={`select-operator-label-${index}`}
                  id="select-operator"
                  value={index === 0 ? '' : operators[index - 1] || ''}
                  label="Select Operator"
                  onChange={event => handleOperatorChange(index - 1, event)}
                  disabled={index === 0}
                >
                  {Object.values(Operator).map(op => (
                    <MenuItem key={op} value={op}>
                      {op}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id={`select-field-label-${index}`}>
                  Select Field
                </InputLabel>
                <Select
                  labelId={`select-field-label-${index}`}
                  id="select-field"
                  value={field}
                  label="Select Field"
                  onChange={event => handleFieldChange(index, event)}
                >
                  {Array.isArray(demographicData) &&
                    demographicData.map(demographicField => (
                      <MenuItem
                        key={demographicField.fieldName}
                        value={demographicField.fieldName}
                        style={{
                          backgroundColor:
                            demographicField.fieldName === field
                              ? 'lightblue'
                              : 'inherit'
                        }}
                      >
                        {transformFieldName(demographicField.fieldName)}
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="select-comparator-label">
                  Select Comparator Function
                </InputLabel>
                <Select
                  labelId={`select-comparator-label-${index}`}
                  id="select-comparator-function"
                  value={comparators[index]}
                  label="Select Comparator Function"
                  onChange={event => handleComparatorChange(index, event)}
                >
                  {options.map(option => (
                    <MenuItem value={option.value} key={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
          ))}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              marginTop: '20px'
            }}
          >
            <IconButton
              id="add-row-button"
              sx={{ marginBottom: '40px', marginTop: '40px' }}
              onClick={handleAddRow}
            >
              <AddIcon />
            </IconButton>
          </Box>
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'flex-start',
              alignItems: 'center',
              marginTop: '40px',
              gap: '20px'
            }}
          >
            <Button
              id="add-rule-button"
              variant="contained"
              size="small"
              onClick={handleAddRule}
              disabled={isAddRuleDisabled()}
            >
              Add Rule
            </Button>
            <Button
              id="close-button"
              variant="contained"
              size="small"
              onClick={handleClose}
            >
              Close
            </Button>
          </Box>
        </>
      )}
    </Box>
  )
}

export default DeterministicContent
