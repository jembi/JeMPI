import {
  Box,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent
} from '@mui/material'
import { useState, useEffect } from 'react'
import { Field, Rule } from 'types/Configuration'
import { transformFieldName } from 'utils/helpers'
import SourceView, { RowData } from './SourceView'



interface DeterministicContentProps {
demographicData: Field[];
linkingRules: {
  link: {
    deterministic: Rule[];
  };
};
}

const options = [
  { value: 0, label: 'Exact' },
  { value: 1, label: 'Low Fuzziness' },
  { value: 2, label: 'Medium Fuzziness' },
  { value: 3, label: 'High Fuzziness' }
]

enum Operator {
  AND = 'And',
  OR = 'Or'
}
const transformRulesToRowData = (rules: any): RowData[] => {
  return rules.link.deterministic.map((rule: any, index:any) => ({
    id: index,
    ruleNumber: index + 1,
    ruleText: rule.text,
  }));
};

const DeterministicContent = ({
  demographicData = [],
  linkingRules
}: DeterministicContentProps) => {
  const [viewType, setViewType] = useState<number>(0)
  const [selectedComparator, setSelectedComparator] = useState<number>(0)
  const [selectedFields, setSelectedFields] = useState<string[]>([])
  const [selectedOperator, setSelectedOperator] = useState<Operator | ''>('')
  const [rules, setRules] = useState<any[]>([])
  const [isOperatorDisabled, setIsOperatorDisabled] = useState<boolean>(true)

  const deterministicRules = transformRulesToRowData(linkingRules);
  
  useEffect(() => {
    const savedComparator = localStorage.getItem('selectedComparator')
    const savedFields = localStorage.getItem('selectedFields')
    const savedOperator = localStorage.getItem('selectedOperator')
    const savedRules = localStorage.getItem('rules')

    if (savedComparator || savedFields || savedOperator || savedRules) {
      const parsedRules = savedRules ? JSON.parse(savedRules) : []
      setRules(parsedRules)
      setSelectedComparator(
        savedComparator ? Number(savedComparator) : selectedComparator
      )
      setSelectedFields(savedFields ? JSON.parse(savedFields) : selectedFields)
      setSelectedOperator(savedOperator ? (savedOperator as Operator) : '')
      setIsOperatorDisabled(parsedRules.length === 0)
    }
  }, [])

  useEffect(() => {
    localStorage.setItem('selectedComparator', selectedComparator.toString())
  }, [selectedComparator])

  useEffect(() => {
    localStorage.setItem('selectedFields', JSON.stringify(selectedFields))
  }, [selectedFields])

  useEffect(() => {
    localStorage.setItem('selectedOperator', selectedOperator)
  }, [selectedOperator])

  useEffect(() => {
    localStorage.setItem('rules', JSON.stringify(rules))
    setIsOperatorDisabled(rules.length === 0)
  }, [rules])

  const handleComparatorChange = (
    event: SelectChangeEvent<typeof selectedComparator>
  ) => {
    setSelectedComparator(event.target.value as number)
  }

  const handleFieldChange = (
    event: SelectChangeEvent<typeof selectedFields>
  ) => {
    setSelectedFields(event.target.value as string[])
  }

  const handleOperatorChange = (
    event: SelectChangeEvent<typeof selectedOperator>
  ) => {
    setSelectedOperator(event.target.value as Operator)
  }

  const handleAddRule = () => {
    const formattedFields = selectedFields.map(field => `eq(${field})`)
    const newRule = {
      vars: selectedFields,
      text: formattedFields.join(
        selectedOperator === Operator.AND ? ' and ' : ' or '
      )
    }
    setRules([...rules, newRule])
    setSelectedFields([])
    setSelectedOperator('')
  }

  return (
    <Box>
      <Box sx={{ mr: 'auto', display: 'flex', flexDirection: 'row', gap: 2, marginBottom:'40px' }}>
      
        <Button
          variant="outlined"
          size="medium"
          onClick={() => setViewType(0)}
          disabled={viewType === 1}
        >
          Source View
        </Button>
        <Button
          variant="outlined"
          size="medium"
          onClick={() => setViewType(1)}
          disabled={false}
        >
          Design View
        </Button>
      </Box>
      {viewType === 0 ?  (
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
          <SourceView data={deterministicRules} onEditRow={function (row: RowData): void {
            throw new Error('Function not implemented.')
          } }/>
        </Box>
      )
      :
      (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'row',
            width: '100%',
            justifyContent: 'center',
            gap: 2
          }}
        >
          <FormControl fullWidth>
            <InputLabel id="select-comparator-label">
              Select Comparator Function
            </InputLabel>
            <Select
              labelId="select-comparator-label"
              id="select-comparator"
              value={selectedComparator}
              label="Select Comparator Function"
              onChange={handleComparatorChange}
            >
              {options.map(option => (
                <MenuItem value={option.value} key={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id="select-field-label">Select Field</InputLabel>
            <Select
              labelId="select-field-label"
              id="select-field"
              value={selectedFields}
              label="Select Field"
              onChange={handleFieldChange}
              multiple
            >
              {Array.isArray(demographicData) &&
                demographicData.map(field => (
                  <MenuItem key={field.fieldName} value={field.fieldName}>
                    {transformFieldName(field.fieldName)}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id="select-operator-label">
              Select Operator
            </InputLabel>
            <Select
              labelId="select-operator-label"
              id="select-operator"
              value={selectedOperator}
              label="Select Operator"
              onChange={handleOperatorChange}
              disabled={isOperatorDisabled}
            >
              {Object.values(Operator).map(op => (
                <MenuItem key={op} value={op}>
                  {op}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      ) }
      <Button
        variant="contained"
        size="small"
        sx={{  marginTop:'40px'}}
        onClick={handleAddRule}
        disabled={
          selectedFields.length === 0 ||
          (selectedFields.length > 1 && !selectedOperator)
        }
      >
        Add Rule
      </Button>
    </Box>
  )
}

export default DeterministicContent