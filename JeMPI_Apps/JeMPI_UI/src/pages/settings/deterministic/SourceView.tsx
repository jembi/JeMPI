import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem,
  OutlinedInput,
  InputLabel,
  FormControl
} from '@mui/material';

type Field = "nationalId" | "givenName" | "familyName" | "phoneNumber" | "dob";

const SourceView: React.FC = () => {
  const [selectedFields, setSelectedFields] = useState<Field[]>([]);

  const handleAddField = (field: Field) => {
    setSelectedFields((prevSelectedFields) => [...prevSelectedFields, field]);
  };

  const handleRemoveField = () => {
    setSelectedFields((prevSelectedFields) => {
      const updatedFields = [...prevSelectedFields];
      updatedFields.pop();
      return updatedFields;
    });
  };

  return (
    <Box
      sx={{
        width: '40%',
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        justifyContent: 'center',
        alignItems: 'center'
      }}
    >
      <FormControl fullWidth>
        <InputLabel id="add-rule-label">Add Rule</InputLabel>
        <Select
          labelId="add-rule-label"
          value=''
          id="add-rule-select"
          sx={{
            width: '50%',
          }}
          input={<OutlinedInput label="Add Rule" />}
          onChange={(e) => handleAddField(e.target.value as Field)}
        >
          <MenuItem value="nationalId">National ID</MenuItem>
          <MenuItem value="givenName">Given Name</MenuItem>
          <MenuItem value="familyName">Family Name</MenuItem>
          <MenuItem value="phoneNumber">Phone Number</MenuItem>
          <MenuItem value="dob">Date of Birth</MenuItem>
        </Select>
      </FormControl>

      {selectedFields.map((field, index) => (
        <TextField
          key={index}
          label={field === 'dob' ? 'Date of Birth' : field}
          InputLabelProps={{
            shrink: true,
          }}
          variant="outlined"
          fullWidth
          margin="normal"
          value=""
          // onChange={(e) => handleInputChange(field, e.target.value)}
        />
      ))}
      {selectedFields.length > 0 && (
        <Button
         variant="outlined" onClick={handleRemoveField}>
          Remove Last Field
        </Button>
      )}
    </Box>
  );
};

export default SourceView;
