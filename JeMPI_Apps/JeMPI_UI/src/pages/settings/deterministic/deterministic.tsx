import { AddOutlined } from '@mui/icons-material';
import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  Typography
} from '@mui/material';
import { useEffect, useState } from 'react';
import { DemographicField } from 'types/Configuration';
import { transformFieldName } from 'utils/helpers';

interface DeterministicProps {
  demographicData: DemographicField[];
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

const Deterministic = ({ demographicData = [] }: DeterministicProps) => {
  const [viewType, setViewType] = useState<number>(0);
  const [selectedComparator, setSelectedComparator] = useState<number>(0);
  const [selectedField, setSelectedField] = useState<string>('');
  const [selectedOperator, setSelectedOperator] = useState<Operator | ''>('');

  useEffect(() => {
    const savedComparator = localStorage.getItem('selectedComparator');
    const savedField = localStorage.getItem('selectedField');
    const savedOperator = localStorage.getItem('selectedOperator');

    if (savedComparator) setSelectedComparator(Number(savedComparator));
    if (savedField) setSelectedField(savedField);
    if (savedOperator) setSelectedOperator(savedOperator as Operator);
  }, []);


  useEffect(() => {
    localStorage.setItem('selectedComparator', selectedComparator.toString());
  }, [selectedComparator]);

  useEffect(() => {
    localStorage.setItem('selectedField', selectedField);
  }, [selectedField]);

  useEffect(() => {
    localStorage.setItem('selectedOperator', selectedOperator);
  }, [selectedOperator]);

  const handleComparatorChange = (event: SelectChangeEvent<typeof selectedComparator>) => {
    setSelectedComparator(event.target.value as number);
  };

  const handleFieldChange = (event: SelectChangeEvent<typeof selectedField>) => {
    setSelectedField(event.target.value);
  };

  const handleOperatorChange = (event: SelectChangeEvent<typeof selectedOperator>) => {
    setSelectedOperator(event.target.value as Operator);
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
                value={selectedField}
                label="Select Field"
                onChange={handleFieldChange}
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
            <Typography variant="h5">eq (National ID)</Typography>
            Or
            <Typography variant="h5">
              eq (given name) and eq(family name, 3) and eq (phone number)
            </Typography>
          </Box>
        )}
      </CardContent>
      <CardActions>
        <IconButton aria-label="add" size="small">
          <AddOutlined fontSize="small" />
        </IconButton>
      </CardActions>
    </Card>
  );
};

export default Deterministic;
