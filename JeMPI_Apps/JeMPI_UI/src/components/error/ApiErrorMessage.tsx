import { Alert, AlertTitle, Container } from '@mui/material';
import { AxiosError } from 'axios';
import { FC } from 'react';

const ApiErrorMessage: FC<{ error?: AxiosError | null }> = ({ error = { message: 'ERROR' } }) => {
  if (!error) {
    return null;
  }

  return (
    <Container>
      <Alert severity="error">
        <AlertTitle>Error</AlertTitle>
        {error.message}
      </Alert>
    </Container>
  );
};

export default ApiErrorMessage;