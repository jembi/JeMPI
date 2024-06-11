import React, { useEffect, useState } from 'react'
import { AddOutlined, DeleteOutline } from '@mui/icons-material'
import {
  Button,
  FormControl,
  InputLabel,
  Select,
  IconButton,
  Box,
  MenuItem,
  SelectChangeEvent
} from '@mui/material'
import SourceView, { RowData } from '../deterministic/SourceView'
import { Configuration, Field, Rule } from 'types/Configuration'
import { Operator, options } from '../deterministic/DeterministicContent'
import { transformFieldName } from 'utils/helpers'
import { useConfiguration } from 'hooks/useUIConfiguration'

interface BlockingContentProps {
  demographicData: Field[]
  hasUndefinedRule: boolean
  linkingRules: {
    link?: { probabilistic?: Rule[] }
    matchNotification?: { probabilistic?: Rule[] }
  }
}

const transformRulesToRowData = (rules: {
  probabilistic: Rule[]
}): RowData[] => {
  return rules.probabilistic.map((rule, index) => ({
    id: index,
    ruleNumber: index + 1,
    ruleText: rule.text
  }))
}

const BlockingContent = ({
  demographicData = [],
  hasUndefinedRule,
  linkingRules
}: BlockingContentProps) => {
  const { configuration, setConfiguration } = useConfiguration()
  const probabilisticRows =
    linkingRules.matchNotification?.probabilistic ??
    linkingRules.link?.probabilistic ??
    []
  const [comparators, setComparators] = useState<number[]>([])
  const [fields, setFields] = useState<string[]>([])
  const [operators, setOperators] = useState<Operator[]>([])
  const [rules, setRules] = useState<Rule[]>([])
  const [editIndex, setEditIndex] = useState<number | null>(null) // Track the index of the rule being edited
  const [initialState, setInitialState] = useState({
    comparators: [] as number[],
    fields: [] as string[],
    operators: [] as Operator[]
  })
  const [hasChanges, setHasChanges] = useState(false) // Track if there are changes

  useEffect(() => {
    console.log('linking rules', linkingRules)
    if (configuration) {
      const initialRules =
        linkingRules.matchNotification?.probabilistic ??
        linkingRules.link?.probabilistic ??
        []
      setRules(initialRules)
    }
  }, [configuration, linkingRules])

  const handleComparatorChange = (
    index: number,
    event: SelectChangeEvent<number>
  ) => {
    const newComparators = [...comparators]
    newComparators[index] = event.target.value as number
    setComparators(newComparators)
    setHasChanges(true)
  }

  const handleFieldChange = (
    index: number,
    event: SelectChangeEvent<string>
  ) => {
    const newFields = [...fields]
    newFields[index] = event.target.value as string
    setFields(newFields)
    setHasChanges(true)
  }

  const handleOperatorChange = (
    index: number,
    event: SelectChangeEvent<Operator>
  ) => {
    const newOperators = [...operators]
    newOperators[index] = event.target.value as Operator
    setOperators(newOperators)
    setHasChanges(true)
  }

  const handleUpdateConfiguration = (
    newRules: Rule[],
    ruleType: 'matchNotification' | 'link'
  ) => {
    setConfiguration(prevConfig => {
      if (!prevConfig) return prevConfig

      const updatedConfig: Configuration = {
        ...prevConfig,
        rules: {
          ...prevConfig.rules,
          [ruleType]: {
            ...prevConfig.rules[ruleType],
            probabilistic: newRules
          }
        }
      }

      localStorage.setItem('configuration', JSON.stringify(updatedConfig))
      console.log('Updated configuration in local storage', updatedConfig)
      return updatedConfig
    })
  }

  const handleAddRule = (ruleType: 'matchNotification' | 'link') => {
    const vars = fields.filter(
      (field, index) => field !== '' && fields.indexOf(field) === index
    )
    const text = vars
      .map((field, index) => {
        const operator =
          index > 0 ? ` ${operators[index - 1].toLowerCase()} ` : ''
        const comparator = comparators[index]
        const comparatorFunction =
          comparator === 0 ? `eq(${field})` : `match(${field}, ${comparator})`
        return `${operator}${comparatorFunction}`
      })
      .join('')

    const newRule: Rule = {
      vars,
      text
    }

    let updatedRules = [...rules]
    if (editIndex !== null) {
      updatedRules[editIndex] = newRule
      setEditIndex(null)
    } else {
      updatedRules = [...rules, newRule]
    }

    handleUpdateConfiguration(updatedRules, ruleType)
    setRules(updatedRules)
    setInitialState({
      comparators: [...comparators],
      fields: [...fields],
      operators: [...operators]
    })
    setHasChanges(false)
  }

  const handleRowEdit = (row: RowData) => {
    const regex = /(?:eq|match)\(([^),]+)(?:,\s*(\d+))?\)/g
    const operatorsRegex = /\s+(and|or)\s+/g
    const matchedFields: string[] = []
    const matchedComparators: number[] = []
    const matchedOperators: string[] = []
    let match

    while ((match = regex.exec(row.ruleText)) !== null) {
      matchedFields.push(match[1])
      matchedComparators.push(match[2] ? parseInt(match[2], 10) : 0)
    }

    let operatorMatch
    while ((operatorMatch = operatorsRegex.exec(row.ruleText)) !== null) {
      matchedOperators.push(operatorMatch[1])
    }

    setFields(matchedFields)
    setComparators(matchedComparators)

    const operatorLength = Math.max(matchedFields.length - 1, 0)
    const filledOperators =
      matchedOperators.length > 0
        ? matchedOperators.slice(0, operatorLength)
        : []
    setOperators(
      filledOperators.map(op => (op === 'or' ? Operator.OR : Operator.AND))
    )
    setEditIndex(row.id)
    setHasChanges(false)
  }

  const handleAddRow = () => {
    setComparators([...comparators, 0])
    setFields([...fields, ''])
    setOperators([...operators, Operator.AND])
    setHasChanges(true)
  }

  const handleClose = () => {
    setEditIndex(null)
    setFields([])
    setComparators([])
    setOperators([])
    setHasChanges(false)
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

    return !hasChanges
  }

  const handleAddUndefinedRule = () => {
    handleAddRow()
    setEditIndex(null)
  }

  const handleDeleteRow = (index: number) => {
    const newFields = fields.filter((_, i) => i !== index)
    const newComparators = comparators.filter((_, i) => i !== index)
    const newOperators = operators.filter((_, i) => i !== index)

    const updatedRules = rules.filter((_, i) => i !== index)

    setFields(newFields)
    setComparators(newComparators)
    setOperators(newOperators)
    setRules(updatedRules)
    setHasChanges(true)

    const ruleType = linkingRules.matchNotification?.probabilistic
      ? 'matchNotification'
      : 'link'
    handleUpdateConfiguration(updatedRules, ruleType)
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
          variant="outlined"
          disabled={editIndex === null}
          size="medium"
          onClick={handleClose}
        >
          Source View
        </Button>
        <Button
          variant="outlined"
          disabled={editIndex !== null}
          size="medium"
          onClick={() => setEditIndex(null)}
        >
          Design View
        </Button>
      </Box>
      {editIndex === null ? (
        <Box
          sx={{
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            gap: 2,
            justifyContent: 'center',
            alignItems: 'center'
          }}
        >
          <SourceView
            data={transformRulesToRowData({ probabilistic: probabilisticRows })}
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
                <InputLabel id={`select-comparator-label-${index}`}>
                  Select Comparator
                </InputLabel>
                <Select
                  labelId={`select-comparator-label-${index}`}
                  id="select-comparator"
                  value={comparators[index]}
                  label="Select Comparator"
                  onChange={event => handleComparatorChange(index, event)}
                >
                  {options.map(option => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <IconButton
                aria-label="delete"
                color="secondary"
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
              alignItems: 'center',
              marginBottom: '40px',
              marginTop: '40px'
            }}
          >
            <IconButton aria-label="add" color="primary" onClick={handleAddRow}>
              <AddOutlined />
            </IconButton>
          </Box>
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center'
            }}
          >
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                const ruleType = linkingRules.matchNotification?.probabilistic
                  ? 'matchNotification'
                  : 'link'
                handleAddRule(ruleType)
              }}
              disabled={isAddRuleDisabled()}
            >
              {editIndex !== null ? 'Save Rule' : 'Add Rule'}
            </Button>
            <Button
              variant="contained"
              color="secondary"
              onClick={handleClose}
              sx={{ marginLeft: '20px' }}
            >
              Close
            </Button>
          </Box>
        </>
      )}
    </Box>
  )
}

export default BlockingContent
