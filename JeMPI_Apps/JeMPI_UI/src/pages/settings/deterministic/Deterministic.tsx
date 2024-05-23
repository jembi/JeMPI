import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  Typography
} from '@mui/material';
import { useEffect, useState } from 'react';
import { DemographicField, Configuration } from 'types/Configuration';
import { transformFieldName } from 'utils/helpers';

interface DeterministicProps {
  demographicData: DemographicField[];
  linkingRules: Configuration['rules']['link']
}

const options = [
  { value: 0, label: 'Exact' },
  { value: 1, label: 'Low Fuzziness' },
  { value: 2, label: 'Medium Fuzziness' },
  { value: 3, label: 'High Fuzziness' }
];

enum Operator {
  AND = 'And',
  OR = 'Or'
}

const Deterministic = ({ demographicData = [], linkingRules }: DeterministicProps) => {
  const [viewType, setViewType] = useState<number>(0);
  const [selectedComparator, setSelectedComparator] = useState<number>(0);
  const [selectedFields, setSelectedFields] = useState<string[]>([]);
  const [selectedOperator, setSelectedOperator] = useState<Operator | ''>('');
  const [rules, setRules] = useState<any[]>([]);
  const [isOperatorDisabled, setIsOperatorDisabled] = useState<boolean>(true);

  const deterministicRules = linkingRules.deterministic || {}
  useEffect(() => {
    const savedComparator = localStorage.getItem('selectedComparator');
    const savedFields = localStorage.getItem('selectedFields');
    const savedOperator = localStorage.getItem('selectedOperator');
    const savedRules = localStorage.getItem('rules');

    if (savedComparator) setSelectedComparator(Number(savedComparator));
    if (savedFields) setSelectedFields(JSON.parse(savedFields));
    if (savedOperator) setSelectedOperator(savedOperator as Operator);
    if (savedRules) {
      const parsedRules = JSON.parse(savedRules);
      setRules(parsedRules);
      setIsOperatorDisabled(parsedRules.length === 0);
    } else {
      setIsOperatorDisabled(true);
    }

    console.log('Loaded rules from storage:', savedRules ? JSON.parse(savedRules) : []);
  }, []);

  useEffect(() => {
    localStorage.setItem('selectedComparator', selectedComparator.toString());
  }, [selectedComparator]);

  useEffect(() => {
    localStorage.setItem('selectedFields', JSON.stringify(selectedFields));
  }, [selectedFields]);

  useEffect(() => {
    localStorage.setItem('selectedOperator', selectedOperator);
  }, [selectedOperator]);

  useEffect(() => {
    localStorage.setItem('rules', JSON.stringify(rules));
    setIsOperatorDisabled(rules.length === 0); // Disable if no rules
    console.log('Current rules:', rules);
  }, [rules]);

  const handleComparatorChange = (event: SelectChangeEvent<typeof selectedComparator>) => {
    setSelectedComparator(event.target.value as number);
  };

  const handleFieldChange = (event: SelectChangeEvent<typeof selectedFields>) => {
    setSelectedFields(event.target.value as string[]);
  };

  const handleOperatorChange = (event: SelectChangeEvent<typeof selectedOperator>) => {
    setSelectedOperator(event.target.value as Operator);
  };

  const handleAddRule = () => {
    const formattedFields = selectedFields.map(field => `eq(${field})`);
    const newRule = {
      vars: selectedFields,
      text: formattedFields.join(selectedOperator === Operator.AND ? ' and ' : ' or ')
    };
    setRules([...rules, newRule]);
    setSelectedFields([]);
    setSelectedOperator('');
  };

  return (
    <Card sx={{ minWidth: 275 }}>
      <CardContent
        sx={{
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          gap: 6
        }}
      >
        <Box sx={{ mr: 'auto', display: 'flex', flexDirection: 'row', gap: 2 }}>
          <Button
            variant="outlined"
            size="medium"
            onClick={() => setViewType(0)}
          >
            Design View
          </Button>
          <Button
            variant="outlined"
            size="medium"
            onClick={() => setViewType(1)}
          >
            Source View
          </Button>
        </Box>
        {viewType === 0 ? (
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
                {options.map((option, index) => (
                  <MenuItem value={option.value} key={index}>
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
                {Array.isArray(demographicData) && demographicData.map((field, index) => (
                  <MenuItem key={index} value={field.fieldName}>
                    {transformFieldName(field.fieldName)}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel id="select-operator-label">Select Operator</InputLabel>
              <Select
                labelId="select-operator-label"
                id="select-operator"
                value={selectedOperator}
                label="Select Operator"
                onChange={handleOperatorChange}
                disabled={isOperatorDisabled}
              >
                {Object.values(Operator).map((op, index) => (
                  <MenuItem key={index} value={op}>
                    {op}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        ) : (
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
            {rules.map((rule, index) => (
              <Typography key={index} variant="h5">
                {`${rule.text}`}
              </Typography>
            ))}
          </Box>
        )}
      </CardContent>
      <CardActions>
        <Button
          variant="contained"
          size="small"
          onClick={handleAddRule}
          disabled={selectedFields.length === 0 || (selectedFields.length > 1 && !selectedOperator)}
        >
          Add Rule
        </Button>
      </CardActions>
    </Card>
  );
};

export default Deterministic;
