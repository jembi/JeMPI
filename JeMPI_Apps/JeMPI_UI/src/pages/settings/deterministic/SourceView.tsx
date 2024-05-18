import React, { useState, ChangeEvent, FC } from 'react';
import { Typography, Box, TextField, Button, MenuItem, Select, FormControl, InputLabel, SelectChangeEvent, OutlinedInput } from '@mui/material';

interface Rule {
  field: string;
  operator: string;
}

const Source: FC = () => {
  const [fieldValue, setFieldValue] = useState<string>('');
  const [selectedField, setSelectedField] = useState<string>('nationalId');
  const [selectedOperator, setSelectedOperator] = useState<string>('and');
  const [rules, setRules] = useState<string[]>([]);

  const handleAddRule = () => {
    let newRule: string;
    if (selectedField === 'nationalId') {
      newRule = `eq(${selectedField})`;
    } else {
      newRule = `eq(${selectedField}) ${selectedOperator} eq(phone number)`;
    }
    setRules([...rules, newRule]);
  };

  const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFieldValue(event.target.value);
  };

  const handleSelectFieldChange = (event: SelectChangeEvent<string>) => {
    setSelectedField(event.target.value as string);
  };

  const handleSelectOperatorChange = (event: SelectChangeEvent<string>) => {
    setSelectedOperator(event.target.value as string);
  };

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
    >
      <FormControl fullWidth>
        <InputLabel>Field</InputLabel>
        <Select
          value={selectedField}
          onChange={handleSelectFieldChange}
          input={<OutlinedInput label="Add Rule" />}
        >
          <MenuItem value="nationalId">National ID</MenuItem>
          <MenuItem value="givenName">Given Name</MenuItem>
          <MenuItem value="familyName">Family Name</MenuItem>
          <MenuItem value="phoneNumber">Phone Number</MenuItem>
        </Select>
      </FormControl>
      <FormControl fullWidth>
        <InputLabel>Operator</InputLabel>
        <Select
          value={selectedOperator}
          onChange={handleSelectOperatorChange}
          input={<OutlinedInput label="Add Rule" />}
        >
          <MenuItem value="and">And</MenuItem>
          <MenuItem value="or">Or</MenuItem>
        </Select>
      </FormControl>
      <Button variant="contained" onClick={handleAddRule}>
        Add Rule
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
      >
        <Typography variant="h6">Generated Rules</Typography>
        {rules.map((rule, index) => (
          <Typography key={index} variant="body1">
            {rule}
          </Typography>
        ))}
      </Box>
    </Box>
  );
};

export default Source;
