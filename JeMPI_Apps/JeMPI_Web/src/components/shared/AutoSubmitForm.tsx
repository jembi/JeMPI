import { debounce } from '@mui/material'
import { Form, useFormikContext } from 'formik'
import React, { FC, ReactNode, useCallback } from 'react'

const AutoSubmitForm: FC<{ children: ReactNode }> = ({ children }) => {
  const { values, submitForm } = useFormikContext()

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const debounceSubmitForm = useCallback(
    debounce((): void => {
      submitForm()
    }, 500),
    [submitForm]
  )

  React.useEffect(() => {
    debounceSubmitForm()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [values])
  return <Form>{children}</Form>
}

export default AutoSubmitForm
