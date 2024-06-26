import React, { useReducer, useCallback, useEffect } from 'react'
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
  handleAddRule?: () => void
  handleDeleteRow?: () => void
  handleRowEdit?: () => void
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

const initialState = {
  comparators: [] as number[],
  fields: [] as string[],
  operators: [] as Operator[],
  rules: [] as Rule[],
  editIndex: null as number | null,
  viewType: 0,
  hasChanges: false
}

type State = typeof initialState
type Action =
  | { type: 'SET_FIELDS'; payload: string[] }
  | { type: 'SET_COMPARATORS'; payload: number[] }
  | { type: 'SET_OPERATORS'; payload: Operator[] }
  | { type: 'SET_RULES'; payload: Rule[] }
  | { type: 'SET_EDIT_INDEX'; payload: number | null }
  | { type: 'SET_VIEW_TYPE'; payload: number }
  | { type: 'SET_HAS_CHANGES'; payload: boolean }
  | { type: 'RESET' }

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case 'SET_FIELDS':
      return { ...state, fields: action.payload }
    case 'SET_COMPARATORS':
      return { ...state, comparators: action.payload }
    case 'SET_OPERATORS':
      return { ...state, operators: action.payload }
    case 'SET_RULES':
      return { ...state, rules: action.payload }
    case 'SET_EDIT_INDEX':
      return { ...state, editIndex: action.payload }
    case 'SET_VIEW_TYPE':
      return { ...state, viewType: action.payload }
    case 'SET_HAS_CHANGES':
      return { ...state, hasChanges: action.payload }
    case 'RESET':
      return initialState
    default:
      return state
  }
}

const BlockingContent = ({
  demographicData = [],
  hasUndefinedRule,
  linkingRules
}: BlockingContentProps) => {
  const { configuration, setConfiguration } = useConfiguration()
  const [state, dispatch] = useReducer(reducer, initialState)
  const [probabilisticRows, setProbabilisticRows] = React.useState<RowData[]>(
    []
  )

  useEffect(() => {
    if (configuration) {
      const initialRules =
        linkingRules.matchNotification?.probabilistic ??
        linkingRules.link?.probabilistic ??
        []
      dispatch({ type: 'SET_RULES', payload: initialRules })
      setProbabilisticRows(
        transformRulesToRowData({ probabilistic: initialRules })
      )
    }
  }, [configuration, linkingRules])

  const handleUpdateConfiguration = useCallback(
    (newRules: Rule[], ruleType: 'matchNotification' | 'link') => {
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
        return updatedConfig
      })

      setProbabilisticRows(transformRulesToRowData({ probabilistic: newRules }))
    },
    [configuration]
  )

  const handleAddRule = useCallback(
    (ruleType: 'matchNotification' | 'link') => {
      const vars = state.fields.filter(
        (field, index) => field !== '' && state.fields.indexOf(field) === index
      )
      const text = state.fields
      .map((field, index) => {
        const operator =
          index > 0 ? ` ${state.operators[index - 1].toLowerCase()} ` : '';
        const comparator = state.comparators[index];
        const comparatorFunction =
          comparator === 0 ? `eq(${field})` : `match(${field}, ${comparator})`;
        return `${operator}${comparatorFunction}`;
      })
      .join('');
       
    
      const newRule: Rule = {
        vars,
        text
      }

      let updatedRules = [...state.rules]
      if (state.editIndex !== null) {
        updatedRules[state.editIndex] = newRule
        dispatch({ type: 'SET_EDIT_INDEX', payload: null })
      } else {
        updatedRules = [...state.rules, newRule]
      }

      handleUpdateConfiguration(updatedRules, ruleType)
      dispatch({ type: 'SET_RULES', payload: updatedRules })
      dispatch({ type: 'SET_HAS_CHANGES', payload: false })
      dispatch({ type: 'SET_VIEW_TYPE', payload: 0 })
    },
    [
      state.fields,
      state.operators,
      state.comparators,
      state.rules,
      state.editIndex,
      handleUpdateConfiguration
    ]
  )

  const handleFieldChange = (
    index: number,
    event: SelectChangeEvent<string>
  ) => {
    const newFields = [...state.fields]
    newFields[index] = event.target.value as string
    dispatch({ type: 'SET_FIELDS', payload: newFields })
    dispatch({ type: 'SET_HAS_CHANGES', payload: true })
  }

  const handleComparatorChange = (
    index: number,
    event: SelectChangeEvent<number>
  ) => {
    const newComparators = [...state.comparators]
    newComparators[index] = event.target.value as number
    dispatch({ type: 'SET_COMPARATORS', payload: newComparators })
    dispatch({ type: 'SET_HAS_CHANGES', payload: true })
  }

  const handleOperatorChange = (
    index: number,
    event: SelectChangeEvent<Operator>
  ) => {
    const newOperators = [...state.operators]
    newOperators[index] = event.target.value as Operator
    dispatch({ type: 'SET_OPERATORS', payload: newOperators })
    dispatch({ type: 'SET_HAS_CHANGES', payload: true })
  }

  const handleRowEdit = (row: RowData) => {
    dispatch({ type: 'SET_VIEW_TYPE', payload: 1 })
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

    dispatch({ type: 'SET_FIELDS', payload: matchedFields })
    dispatch({ type: 'SET_COMPARATORS', payload: matchedComparators })

    const operatorLength = Math.max(matchedFields.length - 1, 0)
    const filledOperators =
      matchedOperators.length > 0
        ? matchedOperators.slice(0, operatorLength)
        : []
    dispatch({
      type: 'SET_OPERATORS',
      payload: filledOperators.map(op =>
        op === 'or' ? Operator.OR : Operator.AND
      )
    })
    dispatch({ type: 'SET_EDIT_INDEX', payload: row.id })
    dispatch({ type: 'SET_HAS_CHANGES', payload: false })
  }

  const handleAddRow = () => {
    dispatch({ type: 'SET_COMPARATORS', payload: [...state.comparators, 0] })
    dispatch({ type: 'SET_FIELDS', payload: [...state.fields, ''] })
    dispatch({
      type: 'SET_OPERATORS',
      payload: [...state.operators, Operator.AND]
    })
    dispatch({ type: 'SET_HAS_CHANGES', payload: true })
  }

  const handleClose = () => {
    dispatch({ type: 'RESET' })
    dispatch({ type: 'SET_VIEW_TYPE', payload: 0 })
  }

  const isAddRuleDisabled = () => {
    if (
      state.fields.length === 0 ||
      state.fields.some(field => field.length === 0) ||
      state.operators.some(
        (operator, index) => index < state.fields.length - 1 && !operator
      )
    ) {
      return true
    }

    return !state.hasChanges
  }

  const handleAddUndefinedRule = () => {
    handleAddRow()
    dispatch({ type: 'SET_VIEW_TYPE', payload: 1 })
  }

  const handleDeleteRow = (index: number) => {
    const newFields = state.fields.filter((_, i) => i !== index)
    const newComparators = state.comparators.filter((_, i) => i !== index)
    const newOperators = state.operators.filter((_, i) => i !== index)
    const updatedRules = state.rules.filter((_, i) => i !== index)
    dispatch({ type: 'SET_FIELDS', payload: newFields })
    dispatch({ type: 'SET_COMPARATORS', payload: newComparators })
    dispatch({ type: 'SET_OPERATORS', payload: newOperators })
    dispatch({ type: 'SET_RULES', payload: updatedRules })
    dispatch({ type: 'SET_HAS_CHANGES', payload: true })

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
          disabled={state.viewType === 1}
          size="medium"
          id="source-view-button"
          onClick={handleClose}
        >
          Source View
        </Button>
        <Button
          variant="outlined"
          disabled={state.viewType === 0}
          size="medium"
          id="design-view-button"
          onClick={() => dispatch({ type: 'SET_VIEW_TYPE', payload: 0 })}
        >
          Design View
        </Button>
      </Box>
      {state.viewType !== 1 ? (
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
            data={probabilisticRows}
            onEditRow={handleRowEdit}
            onAddUndefinedRule={handleAddUndefinedRule}
            hasUndefinedRule={hasUndefinedRule}
          />
        </Box>
      ) : (
        <>
          {state.fields.map((field, index) => (
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
                  value={index === 0 ? '' : state.operators[index - 1] || ''}
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
                  value={state.comparators[index]}
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
              id="add-rule-button"
              onClick={() => {
                const ruleType = linkingRules.matchNotification?.probabilistic
                  ? 'matchNotification'
                  : 'link'
                handleAddRule(ruleType)
              }}
              disabled={isAddRuleDisabled()}
            >
              {state.editIndex !== null ? 'Save Rule' : 'Add Rule'}
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
