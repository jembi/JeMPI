import { useState, useEffect, FC } from 'react'
import {
  Typography,
  Box,
  Button,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  OutlinedInput,
  Chip
} from '@mui/material'
import { SelectChangeEvent } from '@mui/material/Select'
import { toSnakeCase } from 'utils/helpers'

interface Rule {
  fields: string[]
  operator: string
}

const SourceView: FC = () => {
  const [selectedFields, setSelectedFields] = useState<string[]>([])
  const [selectedOperator, setSelectedOperator] = useState<string>('and')
  const [rules, setRules] = useState<Rule[]>([])

  useEffect(() => {
    const savedRules = localStorage.getItem('rules')
    if(savedRules) {
        const parsedRules = JSON.parse(savedRules)
        setRules(parsedRules)
    }
  }, [])

  useEffect(() => {
    localStorage.setItem('rules', JSON.stringify(rules))
  }, [rules])

  const handleAddRule = () => {
    if (selectedFields.length === 0) return

    const newRule: Rule = {
      fields: selectedFields.map(field => toSnakeCase(field)),
      operator: selectedFields.length > 1 ? selectedOperator : ''
    }

    setRules([...rules, newRule])
    setSelectedFields([])
    setSelectedOperator('and')
  }

  const handleSelectFieldChange = (event: SelectChangeEvent<string[]>) => {
    const value = event.target.value as string[]
    setSelectedFields(value)
  }

  const handleSelectOperatorChange = (event: SelectChangeEvent<string>) => {
    setSelectedOperator(event.target.value as string)
  }

  const handleDeleteField = (fieldToDelete: string) => {
    setSelectedFields(selectedFields.filter(field => field !== fieldToDelete))
  }

  const renderRule = (rule: Rule, index: number) => {
    if (rule.fields.length > 1) {
      return (
        <Typography key={index} variant="body1" data-testid={`rule-${index}`}>
          {rule.fields.map(field => `eq(${field})`).join(` ${rule.operator} `)}
        </Typography>
      )
    } else {
      return (
        <Typography key={index} variant="body1" data-testid={`rule-${index}`}>
          eq({rule.fields[0]})
        </Typography>
      )
    }
  }

  return (
    <Box
      sx={{
        width: '50%',
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        justifyContent: 'center',
        alignItems: 'center'
      }}
      data-testid="source-component"
    >
      <FormControl fullWidth>
        <InputLabel id="fields-label">Fields</InputLabel>
        <Select
          labelId="fields-label"
          id="fields-select"
          multiple
          value={selectedFields}
          onChange={handleSelectFieldChange}
          input={<OutlinedInput label="Fields" />}
          renderValue={selected => (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
              {selected.map(value => (
                <Chip
                  key={value}
                  label={value}
                  onDelete={() => handleDeleteField(value)}
                  data-testid={`chip-${value}`}
                />
              ))}
            </Box>
          )}
        >
          <MenuItem value="nationalId">National ID</MenuItem>
          <MenuItem value="givenName">Given Name</MenuItem>
          <MenuItem value="familyName">Family Name</MenuItem>
          <MenuItem value="phoneNumber">Phone Number</MenuItem>
        </Select>
      </FormControl>

      {selectedFields.length > 1 && (
        <FormControl fullWidth>
          <InputLabel id="operator-label">Operator</InputLabel>
          <Select
            labelId="operator-label"
            id="operator-select"
            value={selectedOperator}
            onChange={handleSelectOperatorChange}
            input={<OutlinedInput label="Operator" />}
          >
            <MenuItem value="and">And</MenuItem>
            <MenuItem value="or">Or</MenuItem>
          </Select>
        </FormControl>
      )}

      <Button
        variant="contained"
        onClick={handleAddRule}
        id="add-rule-button"
        data-testid="add-rule-button"
      >
        {' '}
        Add Rule{' '}
      </Button>

      <Box
        sx={{
          width: '50%',
          display: 'flex',
          flexDirection: 'column',
          gap: 1,
          marginTop: 2,
          padding: 2,
          border: '1px solid grey',
          borderRadius: '8px'
        }}
        id="rules-box"
      >
        <Typography variant="h6" id="generated-rules-title">
          Generated Rules
        </Typography>
        {rules.map((rule, index) => (
          <Typography key={index} variant="body1" data-testid={`rule-${index}`}>
            {renderRule(rule, index)}
          </Typography>
        ))}
      </Box>
    </Box>
  )
}

export default SourceView
