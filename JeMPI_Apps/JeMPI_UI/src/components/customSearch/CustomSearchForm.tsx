import { Grid } from '@mui/material'
import AutoSubmitForm from 'components/shared/AutoSubmitForm'
import { FieldArray, Formik } from 'formik'
import { FC } from 'react'
import { CustomSearchQuery, SimpleSearchQuery } from 'types/SimpleSearch'
import { INITIAL_SEARCH_PARAMETER, INITIAL_VALUES } from 'utils/constants'
import AddFieldOrGroupButton from './AddFieldOrGroupButton'
import FieldGroup from './FieldGroup'

const CustomSearchForm: FC<{
  onChange: (values: CustomSearchQuery) => void
}> = ({ onChange }) => {
  return (
    <Formik
      initialValues={INITIAL_VALUES}
      onSubmit={values => onChange(values)}
    >
      {({ values, handleChange, setFieldValue }) => {
        return (
          <AutoSubmitForm>
            <FieldArray name="$or">
              {({ push, remove }) => (
                <>
                  {values.$or.map(
                    (parameters: SimpleSearchQuery, index: number) => {
                      return (
                        <FieldGroup
                          values={parameters}
                          handleChange={handleChange}
                          initialCustomSearchValues={INITIAL_SEARCH_PARAMETER}
                          fieldGroupIndex={index}
                          removeFieldGroup={remove}
                          key={index}
                          setFieldValue={setFieldValue}
                          push={push}
                        />
                      )
                    }
                  )}

                  <Grid
                    item
                    container
                    direction="column"
                    width="100%"
                    alignItems={'center'}
                    sx={{ mt: 1 }}
                  >
                    <Grid
                      item
                      container
                      direction="row"
                      width="756px"
                      justifyContent={'flex-end'}
                    >
                      <AddFieldOrGroupButton
                        onClick={push}
                        initialCustomSearchValues={{
                          parameters: [INITIAL_SEARCH_PARAMETER]
                        }}
                        label="Add group"
                      />
                    </Grid>
                  </Grid>
                </>
              )}
            </FieldArray>
          </AutoSubmitForm>
        )
      }}
    </Formik>
  )
}

export default CustomSearchForm
