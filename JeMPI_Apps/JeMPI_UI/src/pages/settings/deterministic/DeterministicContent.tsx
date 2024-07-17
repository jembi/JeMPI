import { useState, useEffect, useCallback } from 'react'
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
import { Configuration, Field, Rule } from 'types/Configuration'
import { transformFieldName } from 'utils/helpers'
import SourceView, { RowData } from './SourceView'
import { useConfiguration } from 'hooks/useUIConfiguration'
import { DeleteOutline } from '@mui/icons-material'

interface DeterministicContentProps {
  demographicData: Field[]
  hasUndefinedRule: boolean
  linkingRules: {
    link: {
      deterministic: Rule[]
    }
  }
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
}

const useLocalStorageState = <T,>(key: string, defaultValue: T) => {
  const [state, setState] = useState<T>(() => {
    const storedValue = localStorage.getItem(key)
    return storedValue ? JSON.parse(storedValue) : defaultValue
  })

  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(state))
  }, [state, key])

  return [state, setState] as const
}

const DeterministicContent = ({
  demographicData = [],
  linkingRules,
  hasUndefinedRule,
  currentTab
}: DeterministicContentProps) => {
  const [viewType, setViewType] = useState(0)
  const [comparators, setComparators] = useLocalStorageState<number[]>(
    'comparators',
    []
  )
  const [fields, setFields] = useLocalStorageState<string[]>('fields', [])
  const [operators, setOperators] = useLocalStorageState<Operator[]>(
    'operators',
    []
  )
  const [rules, setRules] = useLocalStorageState<Rule[]>('rules', [])
  const [editedRowIndex, setEditedRowIndex] = useState<number>(0)
  const { configuration, setConfiguration } = useConfiguration()
  const deterministicRules = transformRulesToRowData(linkingRules)

  const handleComparatorChange = (
    index: number,
    event: SelectChangeEvent<number>
  ) => {
    const newComparators = [...comparators]
    newComparators[index] = event.target.value as number
    setComparators(newComparators)
  }

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

  const handleAddRow = () => {
    setComparators([...comparators, 0])
    setFields([...fields, ''])
    setOperators([...operators, Operator.AND])
  }

  const handleClose = () => {
    setViewType(0)
  }

  const handleRowEdit = (row: RowData) => {
    if (row.rowIndex !== undefined) {
      setEditedRowIndex(row.rowIndex)
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

  const isAddRuleDisabled = useCallback(() => {
    if (
      fields.length === 0 ||
      fields.some(field => field.length === 0) ||
      operators.some(
        (operator, index) => index < fields.length - 1 && !operator
      )
    ) {
      return true
    }

    return false
  }, [fields, operators])

  const handleAddRule = useCallback(() => {
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

    const rule: Rule = { vars, text }

    handleUpdateConfiguration(rule)

    setRules([...rules, rule])
    setComparators([])
    setFields([])
    setOperators([])
    setViewType(0)
  }, [
    comparators,
    fields,
    operators,
    rules,
    setComparators,
    setFields,
    setOperators,
    setRules
  ])

  const handleAddUndefinedRule = () => {
    handleAddRow()
    setViewType(1)
  }

  const handleUpdateConfiguration = (newRule: Rule) => {
    if (!configuration) return
    const updatedConfiguration = getUpdatedConfiguration(newRule)
    if (updatedConfiguration) {
      setConfiguration(updatedConfiguration)
      localStorage.setItem(
        'configuration',
        JSON.stringify(updatedConfiguration)
      )
    }
  }

  const getUpdatedConfiguration = (newRule: Rule) => {
    if (!configuration) return

    const updatedConfiguration = { ...configuration }
    const ruleType =
      currentTab === 'link'
        ? 'link'
        : currentTab === 'validate'
        ? 'validate'
        : 'matchNotification'

    if (!updatedConfiguration.rules[ruleType]) {
      updatedConfiguration.rules[ruleType] = { deterministic: [] }
    }

    updatedConfiguration.rules[ruleType].deterministic[editedRowIndex] = newRule

    return updatedConfiguration
  }

  const handleDeleteRow = (index: number) => {
    const updateArray = (arr: any[], idx: number) => {
      const newArr = [...arr];
      newArr.splice(idx, 1);
      return newArr;
    };
  
    setComparators(updateArray(comparators, index));
    setFields(updateArray(fields, index));
    if (index > 0) {
      setOperators(updateArray(operators, index - 1));
    }
  
    const updatedConfiguration = { ...configuration };
    const ruleType =
      currentTab === 'link'
        ? 'link'
        : currentTab === 'validate'
        ? 'validate'
        : 'matchNotification';
  
    if (fields.length === 0) {
      const newRules = updateArray(rules, index);
      setRules(newRules);
  
      if (updatedConfiguration.rules?.[ruleType]) {
        updatedConfiguration.rules[ruleType].deterministic.splice(index, 1);
      }
  
      setConfiguration(updatedConfiguration as Configuration);
      localStorage.setItem('configuration', JSON.stringify(updatedConfiguration));
    }
  };
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
                  <MenuItem value="" disabled>
                    Select a field
                  </MenuItem>
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
              <IconButton
                aria-label="delete"
                color="secondary"
                id="delete-button"
                onClick={() => handleDeleteRow(index)}
                sx={{ alignSelf: 'center' }}
              >
                <DeleteOutline />
              </IconButton>
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
