import React, { useState } from 'react';
import { Box, Typography, TextField, Button } from '@mui/material';

const SourceView = () => {
  const [searchParams, setSearchParams] = useState({
    id: '',
    name: '',
    familyName: '',
    phoneNumber: ''
  });
  const [idDisabled, setIdDisabled] = useState(false);

  const handleInputChange = (field: string, value: string) => {
    setSearchParams({ ...searchParams, [field]: value });
  };


  const handleIdChange = (value: string) => {
    setIdDisabled(value !== '');
    handleInputChange('id', value);
  };

  return (
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
      <Typography variant="h5">Search by:</Typography>
      <TextField
        label="National ID"
        value={searchParams.id}
        onChange={(e) => handleIdChange(e.target.value)}
        disabled={idDisabled}
      />
      <Typography variant="body1">Or</Typography>
      <TextField
        label="Given Name"
        value={searchParams.name}
        onChange={(e) => handleInputChange('name', e.target.value)}
        disabled={idDisabled}
      />
      <TextField
        label="Family Name"
        value={searchParams.familyName}
        onChange={(e) => handleInputChange('familyName', e.target.value)}
        disabled={idDisabled}
      />
      <TextField
        label="Phone Number"
        value={searchParams.phoneNumber}
        onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
        disabled={idDisabled}
      />
    </Box>
  );
};

export default SourceView;
